package com.vodovod.service;

import com.vodovod.model.Bill;
import com.vodovod.model.User;
import com.vodovod.model.Payment;
import com.vodovod.repository.BillRepository;
import com.vodovod.repository.PaymentRepository;
import com.vodovod.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void recordPayment(Long userId, Long billId, LocalDate paymentDate, BigDecimal amount, String paymentMethod, String createdBy) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));
        BigDecimal remainingToAllocate = amount;

        if (billId != null) {
            Bill bill = billRepository.findById(billId).orElseThrow(() -> new RuntimeException("Račun nije pronađen"));
            if (!bill.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Odabrani račun ne pripada korisniku");
            }
            BigDecimal remainingOnBill = bill.getTotalAmount().subtract(bill.getPaidAmount());
            BigDecimal allocateToBill = remainingToAllocate.min(remainingOnBill);
            if (allocateToBill.signum() > 0) {
                Payment applied = new Payment(bill, paymentDate, allocateToBill);
                applied.setUser(user);
                applied.setPaymentMethod(paymentMethod);
                applied.setCreatedBy(createdBy);
                paymentRepository.save(applied);

                bill.setPaidAmount(bill.getPaidAmount().add(allocateToBill));
                bill.updateStatus();
                billRepository.save(bill);

                remainingToAllocate = remainingToAllocate.subtract(allocateToBill);
            }
        } else {
            // Ako je odabrano plaćanje unaprijed (billId == null), automatski raspodijeli na najstarije otvorene račune
            List<Bill> openBills = billRepository.findOpenBillsByUser(user);
            for (Bill openBill : openBills) {
                if (remainingToAllocate.signum() <= 0) {
                    break;
                }
                BigDecimal remainingOnBill = openBill.getTotalAmount().subtract(openBill.getPaidAmount());
                BigDecimal allocateToBill = remainingToAllocate.min(remainingOnBill);
                if (allocateToBill.signum() > 0) {
                    Payment applied = new Payment(openBill, paymentDate, allocateToBill);
                    applied.setUser(user);
                    applied.setPaymentMethod(paymentMethod);
                    applied.setCreatedBy(createdBy);
                    paymentRepository.save(applied);

                    openBill.setPaidAmount(openBill.getPaidAmount().add(allocateToBill));
                    openBill.updateStatus();
                    billRepository.save(openBill);

                    remainingToAllocate = remainingToAllocate.subtract(allocateToBill);
                }
            }
        }

        if (remainingToAllocate.signum() > 0) {
            Payment prepayment = new Payment(null, paymentDate, remainingToAllocate);
            prepayment.setUser(user);
            prepayment.setPaymentMethod(paymentMethod);
            prepayment.setCreatedBy(createdBy);
            paymentRepository.save(prepayment);
        }
    }

    @Transactional
    public BigDecimal applyPrepaymentsToBill(Bill bill, String createdBy) {
        User user = bill.getUser();
        BigDecimal remainingOnBill = bill.getTotalAmount().subtract(bill.getPaidAmount());
        if (remainingOnBill.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal availableCredit = paymentRepository.sumPrepaymentByUser(user);
        if (availableCredit == null || availableCredit.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal toApply = availableCredit.min(remainingOnBill);
        if (toApply.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        // Consume prepayments FIFO by reducing their amounts
        BigDecimal remainingToConsume = toApply;
        List<Payment> prepayments = paymentRepository.findByUserAndBillIsNullOrderByPaymentDateAsc(user);
        for (Payment p : prepayments) {
            if (remainingToConsume.signum() <= 0) break;
            BigDecimal take = p.getAmount().min(remainingToConsume);
            BigDecimal newAmount = p.getAmount().subtract(take);
            if (newAmount.signum() == 0) {
                paymentRepository.delete(p);
            } else {
                p.setAmount(newAmount);
                paymentRepository.save(p);
            }
            remainingToConsume = remainingToConsume.subtract(take);
        }

        // Apply as payment to the bill
        Payment applied = new Payment(bill, LocalDate.now(), toApply);
        applied.setUser(user);
        applied.setPaymentMethod("PREPAYMENT");
        applied.setCreatedBy(createdBy);
        paymentRepository.save(applied);

        bill.setPaidAmount(bill.getPaidAmount().add(toApply));
        bill.updateStatus();
        billRepository.save(bill);

        return toApply;
    }
}
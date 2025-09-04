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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
            // Plaćanje bez specificiranog računa: ako postoji otvoreni račun, primijeni SAMO na najstariji;
            // preostali iznos (ako postoji) vodi se kao pretplata.
            List<Bill> openBills = billRepository.findOpenBillsByUser(user);
            if (!openBills.isEmpty() && remainingToAllocate.signum() > 0) {
                Bill oldestOpen = openBills.get(0);
                BigDecimal remainingOnBill = oldestOpen.getTotalAmount().subtract(oldestOpen.getPaidAmount());
                BigDecimal allocateToBill = remainingToAllocate.min(remainingOnBill);
                if (allocateToBill.signum() > 0) {
                    Payment applied = new Payment(oldestOpen, paymentDate, allocateToBill);
                    applied.setUser(user);
                    applied.setPaymentMethod(paymentMethod);
                    applied.setCreatedBy(createdBy);
                    paymentRepository.save(applied);

                    oldestOpen.setPaidAmount(oldestOpen.getPaidAmount().add(allocateToBill));
                    oldestOpen.updateStatus();
                    billRepository.save(oldestOpen);

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
    public void cancelPayment(Long paymentId, String cancelledBy, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Uplata nije pronađena"));

        if (payment.getCancelledAt() != null) {
            return; // već stornirano
        }

        payment.setCancelledAt(LocalDateTime.now());
        payment.setCancelledBy(cancelledBy);
        payment.setCancellationReason(reason);
        paymentRepository.save(payment);

        Bill bill = payment.getBill();
        if (bill != null) {
            // Vrati iznos na računu
            BigDecimal cancelledAmount = payment.getAmount();
            BigDecimal newPaid = bill.getPaidAmount().subtract(cancelledAmount);
            if (newPaid.signum() < 0) {
                newPaid = BigDecimal.ZERO;
            }
            bill.setPaidAmount(newPaid);
            bill.updateStatus();
            billRepository.save(bill);

            // Ako postoji pretplata (avans), automatski primijeni do iznosa stornirane uplate
            BigDecimal remainingOnBill = bill.getTotalAmount().subtract(bill.getPaidAmount());
            if (remainingOnBill.signum() > 0) {
                BigDecimal cap = cancelledAmount.min(remainingOnBill);
                applyPrepaymentsToBillUpTo(bill, cap, cancelledBy);
            }
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
        List<Payment> prepayments = paymentRepository.findByUserAndBillIsNullAndCancelledAtIsNullOrderByPaymentDateAsc(user);
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

    @Transactional
    public BigDecimal applyPrepaymentsToBillUpTo(Bill bill, BigDecimal maxAmount, String createdBy) {
        if (maxAmount == null || maxAmount.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        User user = bill.getUser();
        BigDecimal remainingOnBill = bill.getTotalAmount().subtract(bill.getPaidAmount());
        if (remainingOnBill.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal availableCredit = paymentRepository.sumPrepaymentByUser(user);
        if (availableCredit == null || availableCredit.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal toApply = availableCredit.min(remainingOnBill).min(maxAmount);
        if (toApply.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal remainingToConsume = toApply;
        List<Payment> prepayments = paymentRepository.findByUserAndBillIsNullAndCancelledAtIsNullOrderByPaymentDateAsc(user);
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
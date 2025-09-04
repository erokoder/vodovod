package com.vodovod.service;

import com.vodovod.dto.DashboardStats;
import com.vodovod.dto.UserBalanceDTO;
import com.vodovod.model.BillStatus;
import com.vodovod.model.Role;
import com.vodovod.model.User;
import com.vodovod.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MeterReadingRepository meterReadingRepository;

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // Statistike korisnika
        stats.setTotalUsers(userRepository.countByRoleAndEnabled(Role.USER));
        stats.setActiveUsers(userRepository.findActiveWaterUsers().size());

        // Statistike računa
        stats.setTotalBills(billRepository.count());
        stats.setPaidBills(billRepository.countByStatus(BillStatus.PAID));
        stats.setUnpaidBills(
            billRepository.countByStatus(BillStatus.PENDING) +
            billRepository.countByStatus(BillStatus.PARTIALLY_PAID)
        );
        stats.setOverdueBills(billRepository.findOverdueBills(LocalDate.now()).size());

        // Finansijske statistike
        BigDecimal totalRevenue = paymentRepository.sumTotalAmount();
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        BigDecimal pendingRevenue = billRepository.sumTotalAmountByStatus(BillStatus.PENDING);
        BigDecimal partiallyPaidRevenue = billRepository.sumTotalAmountByStatus(BillStatus.PARTIALLY_PAID);
        BigDecimal totalPending = (pendingRevenue != null ? pendingRevenue : BigDecimal.ZERO)
                .add(partiallyPaidRevenue != null ? partiallyPaidRevenue : BigDecimal.ZERO);
        stats.setPendingRevenue(totalPending);

        // Statistike očitanja
        stats.setTotalReadings(meterReadingRepository.count());
        
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now();
        stats.setReadingsThisMonth(
            meterReadingRepository.countByReadingDateBetween(startOfMonth, endOfMonth)
        );

        // Balansi korisnika
        List<UserBalanceDTO> userBalances = new ArrayList<>();
        userRepository.findByRoleAndEnabledTrue(Role.USER).forEach(user -> {
            BigDecimal totalByUser = billRepository.sumTotalAmountByUser(user);
            BigDecimal paidByUser = billRepository.sumPaidAmountByUser(user);
            BigDecimal prepayments = paymentRepository.sumPrepaymentByUser(user);

            BigDecimal invoicesTotal = totalByUser != null ? totalByUser : BigDecimal.ZERO;
            BigDecimal invoicesPaid = paidByUser != null ? paidByUser : BigDecimal.ZERO;
            BigDecimal credit = prepayments != null ? prepayments : BigDecimal.ZERO;

            // Positive -> duguje; Negative -> pretplata
            BigDecimal netBalance = invoicesTotal.subtract(invoicesPaid).subtract(credit);

            String status;
            BigDecimal displayAmount;
            int cmp = netBalance.compareTo(BigDecimal.ZERO);
            if (cmp > 0) {
                status = "Dužan";
                displayAmount = netBalance;
            } else if (cmp < 0) {
                status = "Pretplaćen";
                displayAmount = netBalance.abs();
            } else {
                status = "Plaćeno sve";
                displayAmount = BigDecimal.ZERO;
            }

            userBalances.add(new UserBalanceDTO(
                    user.getId(),
                    user.getFullName(),
                    user.getMeterNumber(),
                    displayAmount,
                    status
            ));
        });
        stats.setUserBalances(userBalances);

        return stats;
    }
}
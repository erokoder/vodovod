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

    @Autowired
    private CurrentUserService currentUserService;

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        Long orgId = currentUserService.requireCurrentOrganizationId();

        // Statistike korisnika
        stats.setTotalUsers(userRepository.countByOrganizationAndRoleAndEnabled(orgId, Role.USER));
        stats.setActiveUsers(userRepository.findActiveWaterUsers(orgId).size());

        // Statistike računa
        stats.setTotalBills(billRepository.countByOrganizationId(orgId));
        stats.setPaidBills(billRepository.countByStatusAndOrganizationId(BillStatus.PAID, orgId));
        stats.setUnpaidBills(
            billRepository.countByStatusAndOrganizationId(BillStatus.PENDING, orgId) +
            billRepository.countByStatusAndOrganizationId(BillStatus.PARTIALLY_PAID, orgId)
        );
        stats.setOverdueBills(billRepository.findOverdueBillsByOrganizationId(orgId, LocalDate.now()).size());

        // Finansijske statistike
        BigDecimal totalRevenue = paymentRepository.sumTotalAmountByOrganizationId(orgId);
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        BigDecimal pendingRevenue = billRepository.sumTotalAmountByStatusAndOrganizationId(BillStatus.PENDING, orgId);
        BigDecimal partiallyPaidRevenue = billRepository.sumTotalAmountByStatusAndOrganizationId(BillStatus.PARTIALLY_PAID, orgId);
        BigDecimal totalPending = (pendingRevenue != null ? pendingRevenue : BigDecimal.ZERO)
                .add(partiallyPaidRevenue != null ? partiallyPaidRevenue : BigDecimal.ZERO);
        stats.setPendingRevenue(totalPending);

        // Statistike očitanja
        stats.setTotalReadings(meterReadingRepository.countByOrganizationId(orgId));
        
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now();
        stats.setReadingsThisMonth(
            meterReadingRepository.countByOrganizationIdAndReadingDateBetween(orgId, startOfMonth, endOfMonth)
        );

        // Balansi korisnika
        List<UserBalanceDTO> userBalances = new ArrayList<>();
        userRepository.findByOrganizationIdAndRoleAndEnabledTrue(orgId, Role.USER).forEach(user -> {
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
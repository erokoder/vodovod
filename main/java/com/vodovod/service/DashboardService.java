package com.vodovod.service;

import com.vodovod.dto.DashboardStats;
import com.vodovod.model.BillStatus;
import com.vodovod.model.Role;
import com.vodovod.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

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

        return stats;
    }
}
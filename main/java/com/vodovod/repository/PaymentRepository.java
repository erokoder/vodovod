package com.vodovod.repository;

import com.vodovod.model.Bill;
import com.vodovod.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByBillOrderByPaymentDateDesc(Bill bill);
    
    @Query("SELECT p FROM Payment p ORDER BY p.paymentDate DESC")
    List<Payment> findAllOrderByPaymentDateDesc();
    
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC")
    List<Payment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    long countByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p")
    BigDecimal sumTotalAmount();
}
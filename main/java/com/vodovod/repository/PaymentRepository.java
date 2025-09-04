package com.vodovod.repository;

import com.vodovod.model.Bill;
import com.vodovod.model.Payment;
import com.vodovod.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByBillOrderByPaymentDateDesc(Bill bill);
    
    @Query("SELECT p FROM Payment p ORDER BY p.paymentDate DESC")
    List<Payment> findAllOrderByPaymentDateDesc();
    
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC")
    List<Payment> findByPaymentDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByPaymentDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    long countByPaymentDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p")
    BigDecimal sumTotalAmount();
    
    List<Payment> findByUserAndBillIsNullOrderByPaymentDateAsc(User user);
    
    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p WHERE p.user = :user AND p.bill IS NULL")
    BigDecimal sumPrepaymentByUser(@Param("user") User user);

    // Flexible search by optional filters: user and payment date range
    @Query("SELECT p FROM Payment p WHERE (:user IS NULL OR p.user = :user) AND (:startDate IS NULL OR p.paymentDate >= :startDate) AND (:endDate IS NULL OR p.paymentDate <= :endDate) ORDER BY p.paymentDate DESC")
    List<Payment> search(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
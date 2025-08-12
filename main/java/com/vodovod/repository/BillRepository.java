package com.vodovod.repository;

import com.vodovod.model.Bill;
import com.vodovod.model.BillStatus;
import com.vodovod.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    
    List<Bill> findByUserOrderByIssueDateDesc(User user);
    
    List<Bill> findByStatusOrderByIssueDateDesc(BillStatus status);
    
    Optional<Bill> findByBillNumber(String billNumber);
    
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.status = :status")
    long countByStatus(BillStatus status);
    
    @Query("SELECT SUM(b.totalAmount) FROM Bill b WHERE b.status = :status")
    BigDecimal sumTotalAmountByStatus(BillStatus status);
    
    @Query("SELECT SUM(b.paidAmount) FROM Bill b")
    BigDecimal sumPaidAmount();
    
    @Query("SELECT b FROM Bill b WHERE b.dueDate < :date AND b.status NOT IN ('PAID', 'CANCELLED')")
    List<Bill> findOverdueBills(LocalDate date);
    
    @Query("SELECT b FROM Bill b WHERE b.issueDate BETWEEN :startDate AND :endDate ORDER BY b.issueDate DESC")
    List<Bill> findByIssueDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT b FROM Bill b WHERE b.user = :user AND b.status != 'CANCELLED' ORDER BY b.issueDate DESC")
    List<Bill> findActiveByUser(User user);
    
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.issueDate BETWEEN :startDate AND :endDate")
    long countByIssueDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(b.totalAmount) FROM Bill b WHERE b.issueDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalAmountByIssueDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.user = :user AND b.status != 'CANCELLED'")
    BigDecimal sumTotalAmountByUser(User user);

    @Query("SELECT COALESCE(SUM(b.paidAmount), 0) FROM Bill b WHERE b.user = :user AND b.status != 'CANCELLED'")
    BigDecimal sumPaidAmountByUser(User user);

    @Query("SELECT b FROM Bill b WHERE b.user = :user AND b.status IN ('PENDING','PARTIALLY_PAID') ORDER BY b.issueDate ASC")
    List<Bill> findOpenBillsByUser(User user);

    // Returns the maximum numeric prefix (x) in bill_number formatted as x/YYYY for the given year
    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(b.bill_number, 1, LOCATE('/', b.bill_number) - 1) AS INT)), 0) FROM bills b WHERE b.bill_number LIKE CONCAT('%/', :year)", nativeQuery = true)
    int findMaxSequenceForYear(@Param("year") int year);
}
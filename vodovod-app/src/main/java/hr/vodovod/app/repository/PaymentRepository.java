package hr.vodovod.app.repository;

import hr.vodovod.app.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByInvoiceIdOrderByPaymentDateDesc(Long invoiceId);
    
    List<Payment> findByUserIdOrderByPaymentDateDesc(Long userId);
    
    List<Payment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.invoice.id = :invoiceId")
    BigDecimal sumPaymentsByInvoiceId(@Param("invoiceId") Long invoiceId);
    
    @Query("SELECT MAX(CAST(SUBSTRING(p.paymentNumber, LENGTH(:prefix) + 1) AS integer)) FROM Payment p WHERE p.paymentNumber LIKE CONCAT(:prefix, '%')")
    Integer findMaxPaymentNumberByPrefix(@Param("prefix") String prefix);
}
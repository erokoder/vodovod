package hr.vodovod.app.repository;

import hr.vodovod.app.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    List<Invoice> findByUserIdOrderByInvoiceDateDesc(Long userId);
    
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);
    
    List<Invoice> findByStatusAndDueDateBefore(Invoice.InvoiceStatus status, LocalDate date);
    
    @Query("SELECT i FROM Invoice i WHERE i.user.id = :userId AND i.status IN ('UNPAID', 'PARTIALLY_PAID') ORDER BY i.invoiceDate")
    List<Invoice> findUnpaidByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = 'UNPAID'")
    long countUnpaidInvoices();
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = 'PAID'")
    long countPaidInvoices();
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = 'UNPAID'")
    BigDecimal sumUnpaidAmount();
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = 'PAID'")
    BigDecimal sumPaidAmount();
    
    @Query("SELECT MAX(CAST(SUBSTRING(i.invoiceNumber, LENGTH(:prefix) + 1) AS integer)) FROM Invoice i WHERE i.invoiceNumber LIKE CONCAT(:prefix, '%')")
    Integer findMaxInvoiceNumberByPrefix(@Param("prefix") String prefix);
    
    List<Invoice> findByInvoiceDateBetween(LocalDate startDate, LocalDate endDate);
}
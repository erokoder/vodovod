package hr.vodovod.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data
@EqualsAndHashCode(exclude = {"user", "reading", "payments", "stornoInvoice", "originalInvoice"})
@ToString(exclude = {"user", "reading", "payments", "stornoInvoice", "originalInvoice"})
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String invoiceNumber;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToOne
    @JoinColumn(name = "reading_id")
    private Reading reading;
    
    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;
    
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    
    @Column(name = "consumption_m3")
    private BigDecimal consumptionM3;
    
    @Column(name = "price_per_m3", nullable = false)
    private BigDecimal pricePerM3;
    
    @Column(name = "consumption_amount", nullable = false)
    private BigDecimal consumptionAmount;
    
    @Column(name = "fixed_charge")
    private BigDecimal fixedCharge = BigDecimal.ZERO;
    
    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;
    
    @Column(name = "tax_rate", nullable = false)
    private BigDecimal taxRate;
    
    @Column(name = "tax_amount", nullable = false)
    private BigDecimal taxAmount;
    
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
    
    @Column(name = "paid_amount")
    private BigDecimal paidAmount = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.UNPAID;
    
    @Column(name = "is_storno", nullable = false)
    private boolean isStorno = false;
    
    @OneToOne
    @JoinColumn(name = "storno_invoice_id")
    private Invoice stornoInvoice;
    
    @OneToOne(mappedBy = "stornoInvoice")
    private Invoice originalInvoice;
    
    @OneToMany(mappedBy = "invoice")
    private List<Payment> payments = new ArrayList<>();
    
    private String notes;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(paidAmount);
    }
    
    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }
    
    public boolean isOverdue() {
        return !isPaid() && LocalDate.now().isAfter(dueDate);
    }
    
    public enum InvoiceStatus {
        UNPAID, PARTIALLY_PAID, PAID, CANCELLED
    }
}
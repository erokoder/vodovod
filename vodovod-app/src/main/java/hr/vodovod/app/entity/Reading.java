package hr.vodovod.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "readings")
@Data
@EqualsAndHashCode(exclude = {"user", "invoice"})
@ToString(exclude = {"user", "invoice"})
public class Reading {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;
    
    @Column(name = "previous_reading")
    private BigDecimal previousReading;
    
    @Column(name = "current_reading", nullable = false)
    private BigDecimal currentReading;
    
    @Column(name = "consumption")
    private BigDecimal consumption;
    
    @Column(name = "invoice_generated", nullable = false)
    private boolean invoiceGenerated = false;
    
    @OneToOne(mappedBy = "reading")
    private Invoice invoice;
    
    private String notes;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        if (previousReading != null && currentReading != null) {
            consumption = currentReading.subtract(previousReading);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (previousReading != null && currentReading != null) {
            consumption = currentReading.subtract(previousReading);
        }
    }
}
package com.vodovod.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bills")
public class Bill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "bill_number", unique = true)
    private String billNumber; // Broj računa
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "Korisnik je obavezan")
    private User user;
    
    @NotNull(message = "Datum izdavanja je obavezan")
    @Column(name = "issue_date")
    private LocalDate issueDate;
    
    @NotNull(message = "Datum dospijeća je obavezan")
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @Column(name = "period_from")
    private LocalDate periodFrom; // Period obračuna od
    
    @Column(name = "period_to")
    private LocalDate periodTo; // Period obračuna do
    
    @Column(name = "previous_reading", precision = 10, scale = 3)
    private BigDecimal previousReading; // Prethodno očitanje
    
    @Column(name = "current_reading", precision = 10, scale = 3)
    private BigDecimal currentReading; // Trenutno očitanje
    
    @Column(name = "consumption", precision = 10, scale = 3)
    private BigDecimal consumption; // Potrošnja u kubnim metrima
    
    @Column(name = "water_price_per_m3", precision = 10, scale = 2)
    private BigDecimal waterPricePerM3; // Cijena vode po m3
    
    @Column(name = "water_amount", precision = 10, scale = 2)
    private BigDecimal waterAmount; // Iznos za vodu
    
    @Column(name = "fixed_fee", precision = 10, scale = 2)
    private BigDecimal fixedFee; // Pausalna naknada
    
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount; // Ukupan iznos
    
    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO; // Plaćeni iznos
    
    @Enumerated(EnumType.STRING)
    private BillStatus status = BillStatus.PENDING;
    
    @Column(name = "account_number")
    private String accountNumber; // Broj računa za uplate
    
    private String notes; // Napomene
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private String createdBy; // Ko je kreirao račun
    
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancelled_by")
    private String cancelledBy;
    
    @Column(name = "cancellation_reason")
    private String cancellationReason;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public Bill() {}
    
    public Bill(User user, LocalDate issueDate, LocalDate dueDate) {
        this.user = user;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getBillNumber() {
        return billNumber;
    }
    
    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDate getIssueDate() {
        return issueDate;
    }
    
    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public LocalDate getPeriodFrom() {
        return periodFrom;
    }
    
    public void setPeriodFrom(LocalDate periodFrom) {
        this.periodFrom = periodFrom;
    }
    
    public LocalDate getPeriodTo() {
        return periodTo;
    }
    
    public void setPeriodTo(LocalDate periodTo) {
        this.periodTo = periodTo;
    }
    
    public BigDecimal getPreviousReading() {
        return previousReading;
    }
    
    public void setPreviousReading(BigDecimal previousReading) {
        this.previousReading = previousReading;
    }
    
    public BigDecimal getCurrentReading() {
        return currentReading;
    }
    
    public void setCurrentReading(BigDecimal currentReading) {
        this.currentReading = currentReading;
    }
    
    public BigDecimal getConsumption() {
        return consumption;
    }
    
    public void setConsumption(BigDecimal consumption) {
        this.consumption = consumption;
    }
    
    public BigDecimal getWaterPricePerM3() {
        return waterPricePerM3;
    }
    
    public void setWaterPricePerM3(BigDecimal waterPricePerM3) {
        this.waterPricePerM3 = waterPricePerM3;
    }
    
    public BigDecimal getWaterAmount() {
        return waterAmount;
    }
    
    public void setWaterAmount(BigDecimal waterAmount) {
        this.waterAmount = waterAmount;
    }
    
    public BigDecimal getFixedFee() {
        return fixedFee;
    }
    
    public void setFixedFee(BigDecimal fixedFee) {
        this.fixedFee = fixedFee;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getPaidAmount() {
        return paidAmount;
    }
    
    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }
    
    public BillStatus getStatus() {
        return status;
    }
    
    public void setStatus(BillStatus status) {
        this.status = status;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public List<Payment> getPayments() {
        return payments;
    }
    
    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
    
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    
    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
    
    public String getCancelledBy() {
        return cancelledBy;
    }
    
    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    // Utility methods
    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(paidAmount);
    }
    
    public boolean isPaid() {
        return status == BillStatus.PAID;
    }
    
    public boolean isOverdue() {
        return status == BillStatus.OVERDUE || (dueDate.isBefore(LocalDate.now()) && !isPaid());
    }
    
    public void calculateTotal() {
        BigDecimal water = waterAmount != null ? waterAmount : BigDecimal.ZERO;
        BigDecimal fixed = fixedFee != null ? fixedFee : BigDecimal.ZERO;
        this.totalAmount = water.add(fixed);
    }
    
    public void updateStatus() {
        if (status == BillStatus.CANCELLED) {
            return; // Ne mijenjaj status storniranog računa
        }
        
        if (paidAmount.compareTo(totalAmount) >= 0) {
            status = BillStatus.PAID;
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            status = BillStatus.PARTIALLY_PAID;
        } else if (isOverdue()) {
            status = BillStatus.OVERDUE;
        } else {
            status = BillStatus.PENDING;
        }
    }
}
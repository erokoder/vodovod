package com.vodovod.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "Korisnik je obavezan")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = true)
    private Bill bill;
    
    @NotNull(message = "Datum uplate je obavezan")
    @Column(name = "payment_date")
    private LocalDate paymentDate;
    
    @NotNull(message = "Iznos uplate je obavezan")
    @DecimalMin(value = "0.01", message = "Iznos uplate mora biti pozitivan")
    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "payment_method")
    private String paymentMethod; // Način plaćanja (gotovina, bankovni transfer, itd.)
    
    @Column(name = "reference_number")
    private String referenceNumber; // Poziv na broj ili referentni broj uplate
    
    private String notes; // Napomene
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private String createdBy; // Ko je unio uplatu

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
    public Payment() {}
    
    public Payment(Bill bill, LocalDate paymentDate, BigDecimal amount) {
        this.bill = bill;
        this.user = bill != null ? bill.getUser() : null;
        this.paymentDate = paymentDate;
        this.amount = amount;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Bill getBill() {
        return bill;
    }
    
    public void setBill(Bill bill) {
        this.bill = bill;
    }
    
    public LocalDate getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getReferenceNumber() {
        return referenceNumber;
    }
    
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
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
    
    // Utility
    public boolean isPrepayment() {
        return this.bill == null;
    }

    public boolean isCancelled() {
        return this.cancelledAt != null;
    }
}
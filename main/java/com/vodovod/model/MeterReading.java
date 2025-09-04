package com.vodovod.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "meter_readings")
public class MeterReading {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "Korisnik je obavezan")
    private User user;
    
    @NotNull(message = "Datum očitanja je obavezan")
    @Column(name = "reading_date")
    private LocalDate readingDate;
    
    @NotNull(message = "Vrijednost očitanja je obavezna")
    @DecimalMin(value = "0.0", message = "Vrijednost očitanja mora biti pozitivna")
    @Column(name = "reading_value", precision = 10, scale = 3)
    private BigDecimal readingValue;
    
    @Column(name = "previous_reading_value", precision = 10, scale = 3)
    private BigDecimal previousReadingValue;
    
    @Column(name = "consumption", precision = 10, scale = 3)
    private BigDecimal consumption; // Razlika između trenutnog i prethodnog očitanja
    
    @Column(name = "bill_generated")
    private boolean billGenerated = false; // Da li je generiran račun za ovo očitanje

    @Column(name = "cancelled")
    private Boolean cancelled = Boolean.FALSE; // Da li je očitanje stornirano (ne koristi se u izračunima)
    
    private String notes; // Napomene
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private String createdBy; // Ko je unio očitanje
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public MeterReading() {}
    
    public MeterReading(User user, LocalDate readingDate, BigDecimal readingValue) {
        this.user = user;
        this.readingDate = readingDate;
        this.readingValue = readingValue;
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
    
    public LocalDate getReadingDate() {
        return readingDate;
    }
    
    public void setReadingDate(LocalDate readingDate) {
        this.readingDate = readingDate;
    }
    
    public BigDecimal getReadingValue() {
        return readingValue;
    }
    
    public void setReadingValue(BigDecimal readingValue) {
        this.readingValue = readingValue;
    }
    
    public BigDecimal getPreviousReadingValue() {
        return previousReadingValue;
    }
    
    public void setPreviousReadingValue(BigDecimal previousReadingValue) {
        this.previousReadingValue = previousReadingValue;
    }
    
    public BigDecimal getConsumption() {
        return consumption;
    }
    
    public void setConsumption(BigDecimal consumption) {
        this.consumption = consumption;
    }
    
    public boolean isBillGenerated() {
        return billGenerated;
    }
    
    public void setBillGenerated(boolean billGenerated) {
        this.billGenerated = billGenerated;
    }

    public boolean isCancelled() {
        return Boolean.TRUE.equals(cancelled);
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
    
    // Utility methods
    public void calculateConsumption() {
        if (previousReadingValue != null && readingValue != null) {
            this.consumption = readingValue.subtract(previousReadingValue);
        }
    }
}
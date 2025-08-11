package com.vodovod.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class NewReadingDTO {

    @NotNull(message = "Korisnik je obavezan")
    private Long userId;

    @NotNull(message = "Datum očitanja je obavezan")
    @PastOrPresent(message = "Datum očitanja ne može biti u budućnosti")
    private LocalDate readingDate;

    @NotNull(message = "Novo stanje je obavezno")
    @DecimalMin(value = "0.0", message = "Novo stanje mora biti pozitivno")
    @Digits(integer = 7, fraction = 3, message = "Novo stanje može imati maksimalno 7 cijelih i 3 decimalna mjesta")
    private BigDecimal currentReading;

    private String notes;

    // Previous reading value (for display only, not submitted)
    private BigDecimal previousReading;

    // Calculated consumption (for display only)
    private BigDecimal consumption;

    // Constructors
    public NewReadingDTO() {}

    public NewReadingDTO(Long userId, LocalDate readingDate, BigDecimal currentReading) {
        this.userId = userId;
        this.readingDate = readingDate;
        this.currentReading = currentReading;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getReadingDate() {
        return readingDate;
    }

    public void setReadingDate(LocalDate readingDate) {
        this.readingDate = readingDate;
    }

    public BigDecimal getCurrentReading() {
        return currentReading;
    }

    public void setCurrentReading(BigDecimal currentReading) {
        this.currentReading = currentReading;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getPreviousReading() {
        return previousReading;
    }

    public void setPreviousReading(BigDecimal previousReading) {
        this.previousReading = previousReading;
    }

    public BigDecimal getConsumption() {
        return consumption;
    }

    public void setConsumption(BigDecimal consumption) {
        this.consumption = consumption;
    }

    // Utility methods
    public void calculateConsumption() {
        if (previousReading != null && currentReading != null) {
            this.consumption = currentReading.subtract(previousReading);
        }
    }
}
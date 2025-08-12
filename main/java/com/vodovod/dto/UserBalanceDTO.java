package com.vodovod.dto;

import java.math.BigDecimal;

public class UserBalanceDTO {
    private Long userId;
    private String fullName;
    private String meterNumber;
    private BigDecimal amount; // Positive number to display
    private String status; // "Dužan", "Plaćeno sve", or "Pretplaćen"

    public UserBalanceDTO() {}

    public UserBalanceDTO(Long userId, String fullName, String meterNumber, BigDecimal amount, String status) {
        this.userId = userId;
        this.fullName = fullName;
        this.meterNumber = meterNumber;
        this.amount = amount;
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMeterNumber() {
        return meterNumber;
    }

    public void setMeterNumber(String meterNumber) {
        this.meterNumber = meterNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
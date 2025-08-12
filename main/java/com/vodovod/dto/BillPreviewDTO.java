package com.vodovod.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BillPreviewDTO {
    private Long userId;
    private String userName;

    private LocalDate periodFrom;
    private LocalDate periodTo;

    private BigDecimal previousReading;
    private BigDecimal currentReading;
    private BigDecimal consumption;

    private BigDecimal waterPricePerM3;
    private BigDecimal waterAmount;

    private boolean fixedFeeApplied;
    private BigDecimal fixedFeeAmount;

    private BigDecimal totalAmount;

    private LocalDate issueDate;
    private LocalDate dueDate;

    private Long endReadingId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public boolean isFixedFeeApplied() {
        return fixedFeeApplied;
    }

    public void setFixedFeeApplied(boolean fixedFeeApplied) {
        this.fixedFeeApplied = fixedFeeApplied;
    }

    public BigDecimal getFixedFeeAmount() {
        return fixedFeeAmount;
    }

    public void setFixedFeeAmount(BigDecimal fixedFeeAmount) {
        this.fixedFeeAmount = fixedFeeAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

    public Long getEndReadingId() {
        return endReadingId;
    }

    public void setEndReadingId(Long endReadingId) {
        this.endReadingId = endReadingId;
    }
}
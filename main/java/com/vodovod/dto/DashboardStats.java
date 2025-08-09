package com.vodovod.dto;

import java.math.BigDecimal;

public class DashboardStats {
    private long totalUsers;
    private long activeUsers;
    private long totalBills;
    private long paidBills;
    private long unpaidBills;
    private long overdueBills;
    private BigDecimal totalRevenue;
    private BigDecimal pendingRevenue;
    private long totalReadings;
    private long readingsThisMonth;
    
    // Constructors
    public DashboardStats() {}
    
    // Getters and Setters
    public long getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public long getActiveUsers() {
        return activeUsers;
    }
    
    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }
    
    public long getTotalBills() {
        return totalBills;
    }
    
    public void setTotalBills(long totalBills) {
        this.totalBills = totalBills;
    }
    
    public long getPaidBills() {
        return paidBills;
    }
    
    public void setPaidBills(long paidBills) {
        this.paidBills = paidBills;
    }
    
    public long getUnpaidBills() {
        return unpaidBills;
    }
    
    public void setUnpaidBills(long unpaidBills) {
        this.unpaidBills = unpaidBills;
    }
    
    public long getOverdueBills() {
        return overdueBills;
    }
    
    public void setOverdueBills(long overdueBills) {
        this.overdueBills = overdueBills;
    }
    
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public BigDecimal getPendingRevenue() {
        return pendingRevenue;
    }
    
    public void setPendingRevenue(BigDecimal pendingRevenue) {
        this.pendingRevenue = pendingRevenue;
    }
    
    public long getTotalReadings() {
        return totalReadings;
    }
    
    public void setTotalReadings(long totalReadings) {
        this.totalReadings = totalReadings;
    }
    
    public long getReadingsThisMonth() {
        return readingsThisMonth;
    }
    
    public void setReadingsThisMonth(long readingsThisMonth) {
        this.readingsThisMonth = readingsThisMonth;
    }
}
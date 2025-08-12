package com.vodovod.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
public class SystemSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Cijena vode po m3 je obavezna")
    @DecimalMin(value = "0.01", message = "Cijena vode mora biti pozitivna")
    @Column(name = "water_price_per_m3", precision = 10, scale = 2)
    private BigDecimal waterPricePerM3;
    
    @Column(name = "fixed_fee", precision = 10, scale = 2)
    private BigDecimal fixedFee = BigDecimal.ZERO; // Pausalna naknada
    
    @Column(name = "use_fixed_fee")
    private boolean useFixedFee = false; // Da li se koristi pausalna naknada
    
    @NotBlank(message = "Broj računa za uplate je obavezan")
    @Column(name = "account_number")
    private String accountNumber; // Broj računa na koji se uplaćuje
    
    @Column(name = "company_name")
    private String companyName = "Vodovod"; // Naziv tvrtke
    
    @Column(name = "company_address")
    private String companyAddress; // Adresa tvrtke
    
    @Column(name = "company_phone")
    private String companyPhone; // Telefon tvrtke
    
    @Column(name = "company_email")
    private String companyEmail; // Email tvrtke
    
    @Column(name = "bill_due_days")
    private Integer billDueDays = 30; // Broj dana do dospijeća računa
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "updated_by")
    private String updatedBy; // Ko je zadnji ažurirao postavke
    
    @Column(name = "electricity_price_per_kwh", precision = 10, scale = 4)
    @DecimalMin(value = "0.0001", message = "Cijena električne energije mora biti pozitivna")
    private BigDecimal electricityPricePerKWh = new BigDecimal("0.20");

    public BigDecimal getElectricityPricePerKWh() {
        return electricityPricePerKWh;
    }

    public void setElectricityPricePerKWh(BigDecimal electricityPricePerKWh) {
        this.electricityPricePerKWh = electricityPricePerKWh;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public SystemSettings() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public BigDecimal getWaterPricePerM3() {
        return waterPricePerM3;
    }
    
    public void setWaterPricePerM3(BigDecimal waterPricePerM3) {
        this.waterPricePerM3 = waterPricePerM3;
    }
    
    public BigDecimal getFixedFee() {
        return fixedFee;
    }
    
    public void setFixedFee(BigDecimal fixedFee) {
        this.fixedFee = fixedFee;
    }
    
    public boolean isUseFixedFee() {
        return useFixedFee;
    }
    
    public void setUseFixedFee(boolean useFixedFee) {
        this.useFixedFee = useFixedFee;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getCompanyAddress() {
        return companyAddress;
    }
    
    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }
    
    public String getCompanyPhone() {
        return companyPhone;
    }
    
    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }
    
    public String getCompanyEmail() {
        return companyEmail;
    }
    
    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }
    
    public Integer getBillDueDays() {
        return billDueDays;
    }
    
    public void setBillDueDays(Integer billDueDays) {
        this.billDueDays = billDueDays;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
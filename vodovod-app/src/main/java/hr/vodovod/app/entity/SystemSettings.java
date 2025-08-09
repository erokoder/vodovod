package hr.vodovod.app.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
@Data
public class SystemSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "price_per_m3", nullable = false)
    private BigDecimal pricePerM3 = new BigDecimal("1.50");
    
    @Column(name = "fixed_charge")
    private BigDecimal fixedCharge = new BigDecimal("5.00");
    
    @Column(name = "use_fixed_charge", nullable = false)
    private boolean useFixedCharge = true;
    
    @Column(name = "tax_rate", nullable = false)
    private BigDecimal taxRate = new BigDecimal("25.00");
    
    @Column(name = "bank_account_number")
    private String bankAccountNumber;
    
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "company_name")
    private String companyName;
    
    @Column(name = "company_address")
    private String companyAddress;
    
    @Column(name = "company_city")
    private String companyCity;
    
    @Column(name = "company_postal_code")
    private String companyPostalCode;
    
    @Column(name = "company_phone")
    private String companyPhone;
    
    @Column(name = "company_email")
    private String companyEmail;
    
    @Column(name = "company_vat_number")
    private String companyVatNumber;
    
    @Column(name = "invoice_due_days")
    private Integer invoiceDueDays = 15;
    
    @Column(name = "invoice_prefix")
    private String invoicePrefix = "RAC";
    
    @Column(name = "payment_prefix")
    private String paymentPrefix = "UPL";
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
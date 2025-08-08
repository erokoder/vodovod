package com.vodovod.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  private User user;

  @ManyToOne
  private Reading startReading;

  @ManyToOne
  private Reading endReading;

  @Column(nullable = false)
  private LocalDate issuedDate;

  @Column(nullable = false)
  private BigDecimal pricePerM3;

  @Column(nullable = false)
  private boolean flatFeeApplied;

  @Column(nullable = false)
  private BigDecimal flatFeeAmount;

  @Column(nullable = false)
  private Long consumedM3;

  @Column(nullable = false)
  private BigDecimal totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private InvoiceStatus status; // GENERATED, PAID, PARTIALLY_PAID, CANCELED

  @Column
  private String note;

  public static enum InvoiceStatus {
    GENERATED,
    PAID,
    PARTIALLY_PAID,
    CANCELED
  }
}
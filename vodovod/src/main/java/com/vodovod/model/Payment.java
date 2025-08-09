package com.vodovod.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  private Invoice invoice;

  @Column(nullable = false)
  private LocalDate paymentDate;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column
  private String reference;
}
package com.vodovod.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "config_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigSetting {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private BigDecimal pricePerM3;

  @Column(nullable = false)
  private boolean flatFeeEnabled;

  @Column(nullable = false)
  private BigDecimal flatFeeAmount;

  @Column(nullable = false)
  private String iban;
}
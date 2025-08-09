package com.vodovod.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "app.billing")
@Getter
@Setter
public class BillingProperties {
  private BigDecimal pricePerM3;
  private boolean flatFeeEnabled;
  private BigDecimal flatFeeAmount;
  private String iban;
}
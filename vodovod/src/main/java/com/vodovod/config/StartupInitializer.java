package com.vodovod.config;

import com.vodovod.model.ConfigSetting;
import com.vodovod.repository.ConfigSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StartupInitializer implements CommandLineRunner {

  private final ConfigSettingRepository configSettingRepository;
  private final BillingProperties billingProperties;

  @Override
  public void run(String... args) {
    if (configSettingRepository.count() == 0) {
      ConfigSetting cs = ConfigSetting.builder()
          .pricePerM3(billingProperties.getPricePerM3())
          .flatFeeEnabled(billingProperties.isFlatFeeEnabled())
          .flatFeeAmount(billingProperties.getFlatFeeAmount())
          .iban(billingProperties.getIban())
          .build();
      configSettingRepository.save(cs);
    }
  }
}
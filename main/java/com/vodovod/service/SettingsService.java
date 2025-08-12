package com.vodovod.service;

import com.vodovod.model.SystemSettings;
import com.vodovod.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SettingsService {

    @Autowired
    private SystemSettingsRepository systemSettingsRepository;

    public SystemSettings getCurrentSettingsOrNew() {
        return systemSettingsRepository.findTopByOrderByIdAsc().orElseGet(SystemSettings::new);
    }

    public SystemSettings updateSettings(BigDecimal electricityPricePerKWh, BigDecimal fixedFee, boolean useFixedFee) {
        SystemSettings settings = systemSettingsRepository.findTopByOrderByIdAsc().orElseGet(SystemSettings::new);
        settings.setElectricityPricePerKWh(electricityPricePerKWh);
        settings.setFixedFee(fixedFee);
        settings.setUseFixedFee(useFixedFee);
        return systemSettingsRepository.save(settings);
    }
}
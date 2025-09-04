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

    public SystemSettings updateSettings(BigDecimal waterPricePerM3, BigDecimal fixedFee, boolean useFixedFee, String accountNumber,
                                         String companyName, String companyAddress, String companyPhone, String companyOib) {
        SystemSettings settings = systemSettingsRepository.findTopByOrderByIdAsc().orElseGet(SystemSettings::new);
        settings.setWaterPricePerM3(waterPricePerM3);
        settings.setFixedFee(fixedFee);
        settings.setUseFixedFee(useFixedFee);
        settings.setAccountNumber(accountNumber);
        if (companyName != null) settings.setCompanyName(companyName);
        if (companyAddress != null) settings.setCompanyAddress(companyAddress);
        if (companyPhone != null) settings.setCompanyPhone(companyPhone);
        if (companyOib != null) settings.setCompanyOib(companyOib);
        return systemSettingsRepository.save(settings);
    }
}
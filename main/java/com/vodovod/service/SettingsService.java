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

    @Autowired
    private CurrentUserService currentUserService;

    public SystemSettings getCurrentSettingsOrNew() {
        Long orgId = currentUserService.requireCurrentOrganizationId();
        return systemSettingsRepository.findByOrganizationId(orgId).orElseGet(SystemSettings::new);
    }

    public SystemSettings updateSettings(BigDecimal waterPricePerM3, BigDecimal fixedFee, boolean useFixedFee, String accountNumber,
                                         String companyName, String companyAddress, String companyPhone, String companyOib) {
        Long orgId = currentUserService.requireCurrentOrganizationId();
        SystemSettings settings = systemSettingsRepository.findByOrganizationId(orgId).orElseGet(SystemSettings::new);
        settings.setWaterPricePerM3(waterPricePerM3);
        settings.setFixedFee(fixedFee);
        settings.setUseFixedFee(useFixedFee);
        settings.setAccountNumber(accountNumber);
        if (companyName != null) settings.setCompanyName(companyName);
        if (companyAddress != null) settings.setCompanyAddress(companyAddress);
        if (companyPhone != null) settings.setCompanyPhone(companyPhone);
        if (companyOib != null) settings.setCompanyOib(companyOib);
        settings.setOrganization(currentUserService.requireCurrentOrganization());
        return systemSettingsRepository.save(settings);
    }
}
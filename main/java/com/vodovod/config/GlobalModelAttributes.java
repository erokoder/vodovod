package com.vodovod.config;

import com.vodovod.model.SystemSettings;
import com.vodovod.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private SystemSettingsRepository systemSettingsRepository;

    @ModelAttribute("appName")
    public String populateAppName() {
        return systemSettingsRepository.findTopByOrderByIdAsc()
                .map(SystemSettings::getCompanyName)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .orElse("Vodovod Management");
    }
}


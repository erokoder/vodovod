package com.vodovod.config;

import com.vodovod.model.SystemSettings;
import com.vodovod.repository.SystemSettingsRepository;
import com.vodovod.dto.UserBalanceDTO;
import com.vodovod.service.BalanceService;
import com.vodovod.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.core.Authentication;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private SystemSettingsRepository systemSettingsRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BalanceService balanceService;

    @ModelAttribute("appName")
    public String populateAppName() {
        return systemSettingsRepository.findTopByOrderByIdAsc()
                .map(SystemSettings::getCompanyName)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .orElse("Vodovod Management");
    }

    @ModelAttribute("currentUserBalance")
    public UserBalanceDTO populateCurrentUserBalance(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return userService.getUserByUsername(authentication.getName())
                .filter(com.vodovod.model.User::isUser)
                .map(balanceService::getBalanceForUser)
                .orElse(null);
    }
}


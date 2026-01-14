package com.vodovod.config;

import com.vodovod.model.SystemSettings;
import com.vodovod.model.Organization;
import com.vodovod.model.Role;
import com.vodovod.repository.SystemSettingsRepository;
import com.vodovod.dto.UserBalanceDTO;
import com.vodovod.service.BalanceService;
import com.vodovod.service.CurrentUserService;
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

    @Autowired
    private CurrentUserService currentUserService;

    @ModelAttribute("appName")
    public String populateAppName(Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                com.vodovod.model.User cu = currentUserService.requireCurrentUser();
                if (cu.getRole() == Role.SUPER_ADMIN) {
                    return "Vodovod Cloud";
                }
                Organization org = currentUserService.requireCurrentOrganization();
                return systemSettingsRepository.findByOrganizationId(org.getId())
                        .map(SystemSettings::getCompanyName)
                        .map(String::trim)
                        .filter(name -> !name.isEmpty())
                        .orElseGet(() -> org.getName() != null ? org.getName() : "Vodovod Cloud");
            }
        } catch (Exception ignored) {
            // Best-effort fallback for unauthenticated pages / partial migrations
        }
        return "Vodovod Cloud";
    }

    @ModelAttribute("currentOrganizationName")
    public String populateCurrentOrganizationName(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        try {
            com.vodovod.model.User cu = currentUserService.requireCurrentUser();
            if (cu.getRole() == Role.SUPER_ADMIN) {
                return null;
            }
            Organization org = currentUserService.requireCurrentOrganization();
            return org.getName();
        } catch (Exception e) {
            return null;
        }
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

    @ModelAttribute("currentUserFullName")
    public String populateCurrentUserFullName(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return userService.getUserByUsername(authentication.getName())
                .map(com.vodovod.model.User::getFullName)
                .orElse(null);
    }
}


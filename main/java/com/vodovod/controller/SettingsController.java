package com.vodovod.controller;

import com.vodovod.model.SystemSettings;
import com.vodovod.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/settings")
@PreAuthorize("hasRole('ADMIN')")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @GetMapping
    public String index(Model model) {
        SystemSettings settings = settingsService.getCurrentSettingsOrNew();
        model.addAttribute("pageTitle", "Admin Panel");
        model.addAttribute("activeMenu", "settings");
        model.addAttribute("settings", settings);
        return "settings/index";
    }

    @PostMapping
    public String updateSettings(@RequestParam(name = "electricityPricePerKWh") BigDecimal electricityPricePerKWh,
                                 @RequestParam(name = "fixedFee") BigDecimal fixedFee,
                                 @RequestParam(name = "useFixedFee", defaultValue = "false") boolean useFixedFee,
                                 RedirectAttributes redirectAttributes) {
        settingsService.updateSettings(electricityPricePerKWh, fixedFee, useFixedFee);
        redirectAttributes.addFlashAttribute("successMessage", "Postavke su uspješno spremljene.");
        return "redirect:/settings";
    }
}
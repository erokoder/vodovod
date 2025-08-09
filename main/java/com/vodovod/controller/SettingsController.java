package com.vodovod.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/settings")
@PreAuthorize("hasRole('ADMIN')")
public class SettingsController {

    @GetMapping
    public String settings(Model model) {
        model.addAttribute("pageTitle", "Postavke");
        model.addAttribute("activeMenu", "settings");
        
        return "settings/index";
    }

    @GetMapping("/system")
    public String systemSettings(Model model) {
        model.addAttribute("pageTitle", "Sistemske Postavke");
        model.addAttribute("activeMenu", "settings");
        
        return "settings/system";
    }
}
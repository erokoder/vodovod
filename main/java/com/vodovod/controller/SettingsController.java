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
    public String index(Model model) {
        model.addAttribute("pageTitle", "Admin Panel");
        model.addAttribute("activeMenu", "settings");
        return "settings/index";
    }
}
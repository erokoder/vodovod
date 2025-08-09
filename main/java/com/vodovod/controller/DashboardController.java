package com.vodovod.controller;

import com.vodovod.dto.DashboardStats;
import com.vodovod.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(Model model) {
        DashboardStats stats = dashboardService.getDashboardStats();
        
        model.addAttribute("pageTitle", "Pregled");
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("stats", stats);
        
        return "dashboard/index";
    }
}
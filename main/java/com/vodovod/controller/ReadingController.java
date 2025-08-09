package com.vodovod.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/readings")
@PreAuthorize("hasRole('ADMIN')")
public class ReadingController {

    @GetMapping
    public String listReadings(Model model) {
        model.addAttribute("pageTitle", "Očitanja");
        model.addAttribute("activeMenu", "readings");
        
        return "readings/list";
    }

    @GetMapping("/new")
    public String newReadingForm(Model model) {
        model.addAttribute("pageTitle", "Novo Očitanje");
        model.addAttribute("activeMenu", "readings");
        
        return "readings/form";
    }
}
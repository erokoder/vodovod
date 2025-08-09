package com.vodovod.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payments")
@PreAuthorize("hasRole('ADMIN')")
public class PaymentController {

    @GetMapping
    public String listPayments(Model model) {
        model.addAttribute("pageTitle", "Uplate");
        model.addAttribute("activeMenu", "payments");
        
        return "payments/list";
    }

    @GetMapping("/new")
    public String newPaymentForm(Model model) {
        model.addAttribute("pageTitle", "Nova Uplata");
        model.addAttribute("activeMenu", "payments");
        
        return "payments/form";
    }
}
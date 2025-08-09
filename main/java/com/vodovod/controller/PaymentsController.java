package com.vodovod.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payments")
public class PaymentsController {

    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Plaćanja");
        return "payments/index";
    }
    
    @GetMapping("/new")
    public String newPayment(Model model) {
        model.addAttribute("pageTitle", "Novo plaćanje");
        return "payments/new";
    }
}
package com.vodovod.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
  @GetMapping("/")
  public String index(Model model) {
    // Placeholder statistike
    model.addAttribute("numUsers", 0);
    model.addAttribute("numUnpaidInvoices", 0);
    model.addAttribute("numPaidInvoices", 0);
    return "index";
  }
}
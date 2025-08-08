package com.vodovod.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/invoices")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class InvoiceController {
  @GetMapping
  public String index(Model model) {
    return "invoices/index";
  }
}
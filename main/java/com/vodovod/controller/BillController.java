package com.vodovod.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/bills")
@PreAuthorize("hasRole('ADMIN')")
public class BillController {

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "Računi");
        model.addAttribute("activeMenu", "bills");
        return "bills/list";
    }
}
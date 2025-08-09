package com.vodovod.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bills")
public class BillsController {

    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Računi");
        return "bills/index";
    }
}
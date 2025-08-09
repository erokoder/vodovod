package com.vodovod.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/readings")
public class ReadingsController {

    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Očitanja");
        return "readings/index";
    }
    
    @GetMapping("/new")
    public String newReading(Model model) {
        model.addAttribute("pageTitle", "Novo očitanje");
        return "readings/new";
    }
}
package com.vodovod.controller;

import com.vodovod.service.MeterReadingService;
import com.vodovod.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/readings")
public class ReadingsController {

    @Autowired
    private UserService userService;

    @Autowired
    private MeterReadingService meterReadingService;

    /**
     * Prikaz liste očitanja (placeholder)
     */
    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Očitanja");
        return "readings/index";
    }
    
    @GetMapping("/new")
    public String newReading(Model model) {
        model.addAttribute("pageTitle", "Novo očitanje");
        model.addAttribute("users", userService.getActiveWaterUsers());
        return "readings/new";
    }

    /**
     * Spremanje novog očitanja
     */
    @PostMapping("/new")
    public String createReading(@RequestParam("userId") Long userId,
                                @RequestParam("readingDate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate readingDate,
                                @RequestParam("currentReading") java.math.BigDecimal currentReading,
                                @RequestParam(value = "notes", required = false) String notes,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes,
                                Model model) {

        try {
            meterReadingService.createReading(userId, readingDate, currentReading, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Očitanje je uspješno spremljeno.");
            return "redirect:/readings";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "Novo očitanje");
            model.addAttribute("users", userService.getActiveWaterUsers());
            return "readings/new";
        }
    }

    /**
     * API endpoint za dohvat posljednjeg očitanja korisnika
     */
    @GetMapping("/user/{userId}/latest")
    @ResponseBody
    public java.util.Map<String, Object> getLatestReading(@PathVariable Long userId) {
        java.util.Optional<com.vodovod.model.MeterReading> latest = meterReadingService.getLatestReadingByUser(userId);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        latest.ifPresent(mr -> {
            response.put("previousReading", mr.getReadingValue());
            response.put("readingDate", mr.getReadingDate());
        });
        return response;
    }
}
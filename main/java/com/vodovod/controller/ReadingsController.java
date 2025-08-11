package com.vodovod.controller;

import com.vodovod.model.MeterReading;
import com.vodovod.model.User;
import com.vodovod.service.MeterReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/readings")
public class ReadingsController {
    
    @Autowired
    private MeterReadingService meterReadingService;

    @GetMapping
    public String index(Model model) {
        List<MeterReading> readings = meterReadingService.getAllReadings();
        model.addAttribute("readings", readings);
        model.addAttribute("pageTitle", "Očitanja");
        return "readings/index";
    }
    
    @GetMapping("/new")
    public String newReading(Model model) {
        model.addAttribute("pageTitle", "Novo očitanje");
        model.addAttribute("users", meterReadingService.getActiveWaterUsers());
        return "readings/new";
    }
    
    @PostMapping("/new")
    public String createReading(
            @RequestParam Long userId,
            @RequestParam String readingDate,
            @RequestParam BigDecimal currentReading,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Dohvati korisnika
            Optional<User> userOpt = meterReadingService.getUserById(userId);
            if (!userOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Korisnik nije pronađen.");
                return "redirect:/readings/new";
            }
            
            User user = userOpt.get();
            LocalDate date = LocalDate.parse(readingDate);
            
            // Validiraj očitanje
            MeterReadingService.ValidationResult validation = 
                meterReadingService.validateNewReading(user, date, currentReading);
            
            if (!validation.isValid()) {
                redirectAttributes.addFlashAttribute("error", validation.getErrorMessage());
                return "redirect:/readings/new";
            }
            
            // Spremi očitanje
            MeterReading reading = meterReadingService.saveReading(user, date, currentReading, notes);
            
            redirectAttributes.addFlashAttribute("success", 
                String.format("Očitanje za korisnika %s %s je uspješno spremljeno.", 
                    user.getFirstName(), user.getLastName()));
            
            return "redirect:/readings";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Greška prilikom spremanja očitanja: " + e.getMessage());
            return "redirect:/readings/new";
        }
    }
    
    // API endpoints za AJAX pozive
    
    @GetMapping("/api/users")
    @ResponseBody
    public List<Map<String, Object>> getUsers() {
        List<User> users = meterReadingService.getActiveWaterUsers();
        return users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("name", user.getFirstName() + " " + user.getLastName());
            userMap.put("address", user.getAddress());
            userMap.put("meterNumber", user.getMeterNumber());
            return userMap;
        }).collect(Collectors.toList());
    }
    
    @GetMapping("/api/user/{userId}/last-reading")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLastReading(@PathVariable Long userId) {
        Optional<User> userOpt = meterReadingService.getUserById(userId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        Optional<MeterReading> lastReading = meterReadingService.getLatestReadingForUser(user);
        
        Map<String, Object> response = new HashMap<>();
        if (lastReading.isPresent()) {
            MeterReading reading = lastReading.get();
            response.put("hasLastReading", true);
            response.put("lastReadingValue", reading.getReadingValue());
            response.put("lastReadingDate", reading.getReadingDate().toString());
        } else {
            response.put("hasLastReading", false);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/api/validate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateReading(
            @RequestParam Long userId,
            @RequestParam String readingDate,
            @RequestParam BigDecimal currentReading) {
        
        Map<String, Object> response = new HashMap<>();
        
        Optional<User> userOpt = meterReadingService.getUserById(userId);
        if (!userOpt.isPresent()) {
            response.put("valid", false);
            response.put("error", "Korisnik nije pronađen.");
            return ResponseEntity.ok(response);
        }
        
        User user = userOpt.get();
        LocalDate date = LocalDate.parse(readingDate);
        
        MeterReadingService.ValidationResult validation = 
            meterReadingService.validateNewReading(user, date, currentReading);
        
        response.put("valid", validation.isValid());
        if (!validation.isValid()) {
            response.put("errors", validation.getErrors());
        }
        if (validation.getPreviousReading() != null) {
            response.put("previousValue", validation.getPreviousReading().getReadingValue());
            response.put("consumption", currentReading.subtract(validation.getPreviousReading().getReadingValue()));
        }
        
        return ResponseEntity.ok(response);
    }
}
package com.vodovod.controller;

import com.vodovod.dto.NewReadingDTO;
import com.vodovod.model.MeterReading;
import com.vodovod.model.Role;
import com.vodovod.model.User;
import com.vodovod.service.MeterReadingService;
import com.vodovod.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/readings")
public class ReadingsController {

    @Autowired
    private UserService userService;

    @Autowired
    private MeterReadingService meterReadingService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Očitanja");
        model.addAttribute("activeMenu", "readings");
        model.addAttribute("readings", meterReadingService.getAllReadings());
        return "readings/index";
    }
    
    @GetMapping("/new")
    public String newReading(@RequestParam(value = "userId", required = false) Long userId, Model model) {
        model.addAttribute("pageTitle", "Novo očitanje");
        NewReadingDTO dto = new NewReadingDTO();
        if (userId != null) {
            dto.setUserId(userId);
        }
        model.addAttribute("newReadingDTO", dto);
        
        // Dodaj listu korisnika za dropdown
        List<User> users = userService.findByRoleAndEnabledTrue(Role.USER);
        model.addAttribute("users", users);
        
        return "readings/new";
    }

    @PostMapping("/new")
    public String saveReading(@Valid @ModelAttribute NewReadingDTO newReadingDTO, 
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes,
                            Authentication authentication) {
        
        // Ako ima grešaka u validaciji, vrati na formu
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Novo očitanje");
            List<User> users = userService.findByRoleAndEnabledTrue(Role.USER);
            model.addAttribute("users", users);
            return "readings/new";
        }

        try {
            // Pronađi korisnika
            User user = userService.findById(newReadingDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

            // Validiraj novo očitanje
            if (!meterReadingService.isValidNewReading(user, newReadingDTO.getCurrentReading(), newReadingDTO.getReadingDate())) {
                bindingResult.rejectValue("currentReading", "error.invalidReading", 
                    "Novo očitanje mora biti veće od prethodnog i datum mora biti noviji");
                model.addAttribute("pageTitle", "Novo očitanje");
                List<User> users = userService.findByRoleAndEnabledTrue(Role.USER);
                model.addAttribute("users", users);
                return "readings/new";
            }

            // Stvori novo očitanje
            MeterReading reading = new MeterReading();
            reading.setUser(user);
            reading.setReadingDate(newReadingDTO.getReadingDate());
            reading.setReadingValue(newReadingDTO.getCurrentReading());
            reading.setNotes(newReadingDTO.getNotes());
            reading.setCreatedBy(authentication.getName());

            // Spremi očitanje
            meterReadingService.saveReading(reading);

            redirectAttributes.addFlashAttribute("successMessage", "Očitanje je uspješno spremljeno!");
            return "redirect:/readings";

        } catch (Exception e) {
            bindingResult.rejectValue("currentReading", "error.saveReading", 
                "Dogodila se greška prilikom spremanja očitanja: " + e.getMessage());
            model.addAttribute("pageTitle", "Novo očitanje");
            List<User> users = userService.findByRoleAndEnabledTrue(Role.USER);
            model.addAttribute("users", users);
            return "readings/new";
        }
    }

    /**
     * API endpoint za dohvaćanje prethodnog očitanja korisnika
     */
    @GetMapping("/api/user/{userId}/latest-reading")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLatestReading(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

            Optional<MeterReading> latestReading = meterReadingService.getLatestReadingByUser(user);
            
            Map<String, Object> response = new HashMap<>();
            if (latestReading.isPresent()) {
                MeterReading reading = latestReading.get();
                response.put("hasReading", true);
                response.put("previousReading", reading.getReadingValue());
                response.put("lastReadingDate", reading.getReadingDate());
            } else {
                response.put("hasReading", false);
                response.put("previousReading", BigDecimal.ZERO);
                response.put("lastReadingDate", null);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * API endpoint za validaciju novog očitanja
     */
    @PostMapping("/api/validate-reading")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateReading(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            BigDecimal newReading = new BigDecimal(request.get("newReading").toString());
            LocalDate readingDate = LocalDate.parse(request.get("readingDate").toString());

            User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

            boolean isValid = meterReadingService.isValidNewReading(user, newReading, readingDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            if (!isValid) {
                Optional<MeterReading> latestReading = meterReadingService.getLatestReadingByUser(user);
                if (latestReading.isPresent()) {
                    if (newReading.compareTo(latestReading.get().getReadingValue()) <= 0) {
                        response.put("message", "Novo očitanje mora biti veće od prethodnog (" + latestReading.get().getReadingValue() + " m³)");
                    } else if (!readingDate.isAfter(latestReading.get().getReadingDate())) {
                        response.put("message", "Datum očitanja mora biti noviji od prethodnog (" + latestReading.get().getReadingDate() + ")");
                    }
                }
                if (meterReadingService.existsReadingForUserAndDate(user, readingDate)) {
                    response.put("message", "Već postoji očitanje za ovaj datum");
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
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
import java.math.RoundingMode;

@Controller
@RequestMapping("/readings")
public class ReadingsController {

    @Autowired
    private UserService userService;

    @Autowired
    private MeterReadingService meterReadingService;

    @GetMapping
    public String index(@RequestParam(value = "userId", required = false) Long userId,
                        @RequestParam(value = "fromDate", required = false) String fromDateStr,
                        @RequestParam(value = "toDate", required = false) String toDateStr,
                        Model model) {
        List<MeterReading> readings;
        LocalDate fromDate = null;
        LocalDate toDate = null;
        if (fromDateStr != null && !fromDateStr.isBlank()) {
            fromDate = LocalDate.parse(fromDateStr);
        }
        if (toDateStr != null && !toDateStr.isBlank()) {
            toDate = LocalDate.parse(toDateStr);
        }

        if (userId != null) {
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isPresent()) {
                // For admin view, show all readings including cancelled ones
                if (fromDate == null && toDate == null) {
                    readings = meterReadingService.getReadingsByUserIncludingCancelled(userOpt.get());
                } else {
                    readings = meterReadingService.getReadingsByUserAndDateRange(userOpt.get(), fromDate, toDate);
                }
                model.addAttribute("selectedUserId", userId);
            } else {
                if (fromDate == null && toDate == null) {
                    readings = meterReadingService.getAllReadingsIncludingCancelled();
                } else {
                    readings = meterReadingService.getAllReadingsByDateRange(fromDate, toDate);
                }
            }
        } else {
            if (fromDate == null && toDate == null) {
                readings = meterReadingService.getAllReadingsIncludingCancelled();
            } else {
                readings = meterReadingService.getAllReadingsByDateRange(fromDate, toDate);
            }
        }
        model.addAttribute("pageTitle", "Očitanja");
        model.addAttribute("readings", readings);
        // Add users for filter dropdown (only active water users)
        model.addAttribute("users", userService.getActiveWaterUsers());
        // Preserve selected filters
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "readings/index";
    }
    
    @GetMapping("/new")
    public String newReading(Model model) {
        model.addAttribute("pageTitle", "Novo očitanje");
        NewReadingDTO dto = new NewReadingDTO();
        dto.setReadingDate(LocalDate.now());
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
     * Detaljni prikaz pojedinačnog očitanja
     */
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        MeterReading reading = meterReadingService.findById(id)
                .orElseThrow(() -> new RuntimeException("Očitanje nije pronađeno"));
        model.addAttribute("pageTitle", "Pregled očitanja #" + id);
        model.addAttribute("reading", reading);
        return "readings/view";
    }

    /**
     * API endpoint za dohvaćanje prethodnog očitanja korisnika
     */
    @GetMapping("/api/user/{userId}/latest-reading")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> getLatestReading(@PathVariable Long userId) {
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
            
            // Ako nije validno, vrati poruku zašto
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
            } else {
                // Ako je validno, provjeri odstupanje potrošnje > 20% i postavi upozorenje
                Optional<MeterReading> latestReading = meterReadingService.getLatestReadingByUser(user);
                if (latestReading.isPresent()) {
                    MeterReading last = latestReading.get();
                    BigDecimal previousOfLast = last.getPreviousReadingValue();
                    if (previousOfLast != null) {
                        BigDecimal lastConsumption = last.getReadingValue().subtract(previousOfLast);
                        if (lastConsumption.signum() > 0) {
                            BigDecimal newConsumption = newReading.subtract(last.getReadingValue());
                            if (newConsumption.signum() > 0) {
                                BigDecimal diff = newConsumption.subtract(lastConsumption).abs();
                                BigDecimal percent = diff.divide(lastConsumption, 6, java.math.RoundingMode.HALF_UP);
                                if (percent.compareTo(new BigDecimal("0.20")) > 0) {
                                    response.put("warn", true);
                                    response.put("warningMessage", "Nova potrošnja (" + newConsumption + " m³) odstupa više od 20% od prethodne (" + lastConsumption + " m³)");
                                }
                            }
                        }
                    }
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Stornira očitanje
     */
    @PostMapping("/{id}/cancel")
    public String cancelReading(@PathVariable Long id,
                              @RequestParam String reason,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            // Provjeri da li se očitanje može stornirati
            if (!meterReadingService.canCancelReading(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Očitanje se ne može stornirati. Možda je već stornirano ili je već generiran račun.");
                return "redirect:/readings/" + id;
            }

            // Storniraj očitanje
            meterReadingService.cancelReading(id, authentication.getName(), reason);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Očitanje je uspješno stornirano!");
            return "redirect:/readings/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Greška prilikom storniranja očitanja: " + e.getMessage());
            return "redirect:/readings/" + id;
        }
    }

    /**
     * API endpoint za provjeru da li se očitanje može stornirati
     */
    @GetMapping("/api/{id}/can-cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> canCancelReading(@PathVariable Long id) {
        try {
            boolean canCancel = meterReadingService.canCancelReading(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("canCancel", canCancel);
            
            if (!canCancel) {
                Optional<MeterReading> reading = meterReadingService.findById(id);
                if (reading.isPresent()) {
                    if (reading.get().isCancelled()) {
                        response.put("reason", "Očitanje je već stornirano");
                    } else if (reading.get().isBillGenerated()) {
                        response.put("reason", "Račun je već generiran za ovo očitanje");
                    }
                } else {
                    response.put("reason", "Očitanje nije pronađeno");
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
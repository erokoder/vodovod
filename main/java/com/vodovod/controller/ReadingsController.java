package com.vodovod.controller;

import com.vodovod.model.MeterReading;
import com.vodovod.model.User;
import com.vodovod.repository.MeterReadingRepository;
import com.vodovod.service.UserService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
    private MeterReadingRepository meterReadingRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Očitanja");
        model.addAttribute("activeMenu", "readings");
        return "readings/index";
    }
    
    @GetMapping("/new")
    public String newReading(Model model) {
        List<User> users = userService.getActiveWaterUsers();
        model.addAttribute("pageTitle", "Novo očitanje");
        model.addAttribute("activeMenu", "readings");
        model.addAttribute("users", users);
        return "readings/new";
    }

    @PostMapping("/new")
    public String createReading(@RequestParam("userId") @NotNull Long userId,
                                @RequestParam("readingDate") @NotNull LocalDate readingDate,
                                @RequestParam("currentReading") @NotNull BigDecimal currentReading,
                                @RequestParam(value = "notes", required = false) String notes,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Korisnik nije pronađen.");
            return newReading(model);
        }

        User user = userOpt.get();
        Optional<MeterReading> latestOpt = meterReadingRepository.findTopByUserOrderByReadingDateDesc(user);

        // Validacija: ako postoji zadnje očitanje, novo mora biti veće
        if (latestOpt.isPresent()) {
            MeterReading latest = latestOpt.get();
            if (currentReading.compareTo(latest.getReadingValue()) <= 0) {
                model.addAttribute("errorMessage", "Novo očitanje mora biti veće od posljednjeg očitanja (" + latest.getReadingValue() + ").");
                return newReading(model);
            }
            // Opcionalno: spriječi unos očitanja s datumom prije zadnjeg
            if (readingDate.isBefore(latest.getReadingDate())) {
                model.addAttribute("errorMessage", "Datum očitanja ne može biti prije posljednjeg očitanja (" + latest.getReadingDate() + ").");
                return newReading(model);
            }
        }

        // Kreiraj i izračunaj potrošnju
        MeterReading meterReading = new MeterReading();
        meterReading.setUser(user);
        meterReading.setReadingDate(readingDate);
        meterReading.setReadingValue(currentReading);
        meterReading.setNotes(notes);

        latestOpt.ifPresent(latest -> meterReading.setPreviousReadingValue(latest.getReadingValue()));
        meterReading.calculateConsumption();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            meterReading.setCreatedBy(authentication.getName());
        }

        meterReadingRepository.save(meterReading);
        redirectAttributes.addFlashAttribute("successMessage", "Očitanje je uspješno spremljeno.");
        return "redirect:/readings";
    }

    @GetMapping("/latest")
    @ResponseBody
    public Map<String, Object> getLatestReading(@RequestParam("userId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Korisnik nije pronađen");
            return response;
        }

        Optional<MeterReading> latestOpt = meterReadingRepository.findTopByUserOrderByReadingDateDesc(userOpt.get());
        if (latestOpt.isPresent()) {
            MeterReading latest = latestOpt.get();
            response.put("success", true);
            response.put("readingDate", latest.getReadingDate());
            response.put("readingValue", latest.getReadingValue());
        } else {
            response.put("success", true);
            response.put("readingDate", null);
            response.put("readingValue", BigDecimal.ZERO);
        }
        return response;
    }
}
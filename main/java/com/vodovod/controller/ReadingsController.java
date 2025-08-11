package com.vodovod.controller;

import com.vodovod.dto.UserDto;
import com.vodovod.model.MeterReading;
import com.vodovod.model.User;
import com.vodovod.repository.MeterReadingRepository;
import com.vodovod.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/readings")
public class ReadingsController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MeterReadingRepository meterReadingRepository;

    @GetMapping
    public String index(Model model) {
        List<MeterReading> readings = meterReadingRepository.findAllByOrderByReadingDateDesc();
        model.addAttribute("readings", readings);
        model.addAttribute("pageTitle", "Očitanja");
        return "readings/index";
    }
    
    @GetMapping("/new")
    public String newReading(Model model) {
        List<User> users = userRepository.findActiveWaterUsers();
        model.addAttribute("users", users);
        model.addAttribute("pageTitle", "Novo očitanje");
        return "readings/new";
    }
    
    @PostMapping("/new")
    public String saveReading(@RequestParam Long userId,
                             @RequestParam String readingDate,
                             @RequestParam BigDecimal currentReading,
                             @RequestParam(required = false) String notes,
                             RedirectAttributes redirectAttributes) {
        
        try {
            // Validacija korisnika
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Odabrani korisnik ne postoji.");
                return "redirect:/readings/new";
            }
            User user = userOpt.get();
            
            // Provjera da li već postoji očitanje za taj datum
            LocalDate date = LocalDate.parse(readingDate);
            Optional<MeterReading> existingReading = meterReadingRepository.findByUserAndReadingDate(user, date);
            if (existingReading.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Već postoji očitanje za odabrani datum.");
                return "redirect:/readings/new";
            }
            
            // Dohvaćanje zadnjeg očitanja
            Optional<MeterReading> lastReading = meterReadingRepository.findLatestByUser(user);
            BigDecimal previousReading = BigDecimal.ZERO;
            
            if (lastReading.isPresent()) {
                MeterReading last = lastReading.get();
                previousReading = last.getReadingValue();
                
                // Validacija da novo očitanje mora biti veće od prethodnog
                if (currentReading.compareTo(previousReading) <= 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Novo očitanje (" + currentReading + " m³) mora biti veće od prethodnog (" + previousReading + " m³).");
                    return "redirect:/readings/new";
                }
            }
            
            // Kreiranje novog očitanja
            MeterReading newReading = new MeterReading();
            newReading.setUser(user);
            newReading.setReadingDate(date);
            newReading.setReadingValue(currentReading);
            newReading.setPreviousReadingValue(previousReading);
            newReading.setNotes(notes);
            newReading.setCreatedBy("admin"); // TODO: Dohvatiti iz security contexta
            
            // Izračun potrošnje
            newReading.calculateConsumption();
            
            meterReadingRepository.save(newReading);
            
            redirectAttributes.addFlashAttribute("successMessage", "Očitanje je uspješno spremljeno.");
            return "redirect:/readings";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Greška pri spremanju očitanja: " + e.getMessage());
            return "redirect:/readings/new";
        }
    }
    
    @GetMapping("/api/users")
    @ResponseBody
    public List<UserDto> getUsers() {
        List<User> users = userRepository.findActiveWaterUsers();
        return users.stream()
                .map(user -> new UserDto(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getMeterNumber(),
                    user.getEmail(),
                    user.isEnabled()
                ))
                .collect(Collectors.toList());
    }
    
    @GetMapping("/api/last-reading/{userId}")
    @ResponseBody
    public MeterReading getLastReading(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            Optional<MeterReading> lastReading = meterReadingRepository.findLatestByUser(userOpt.get());
            return lastReading.orElse(null);
        }
        return null;
    }
}
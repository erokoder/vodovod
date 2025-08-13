package com.vodovod.controller;

import com.vodovod.model.User;
import com.vodovod.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class MyAccountController {

    @Autowired
    private UserService userService;

    @GetMapping("/my-bills")
    public String myBills(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.getUserByUsername(username);
        
        if (userOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Korisnik nije pronađen.");
            return "redirect:/login";
        }

        User user = userOpt.get();
        model.addAttribute("pageTitle", "Moji Računi");
        model.addAttribute("activeMenu", "my-bills");
        model.addAttribute("user", user);
        
        return "account/my-bills";
    }

    @GetMapping("/my-account")
    public String myAccount(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.getUserByUsername(username);
        
        if (userOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Korisnik nije pronađen.");
            return "redirect:/login";
        }

        User user = userOpt.get();
        model.addAttribute("pageTitle", "Moj Račun");
        model.addAttribute("activeMenu", "my-account");
        model.addAttribute("user", user);

        // Basic dashboard data for the user
        java.math.BigDecimal prepayment = userService.getPrepaymentBalanceForUser(user);
        model.addAttribute("prepaymentBalance", prepayment);

        java.util.List<com.vodovod.model.Bill> userBills = userService.getBillsForUser(user);
        long openBillsCount = userBills.stream()
            .filter(b -> b.getStatus() == com.vodovod.model.BillStatus.PENDING || b.getStatus() == com.vodovod.model.BillStatus.PARTIALLY_PAID)
            .count();
        java.math.BigDecimal openBillsTotal = userBills.stream()
            .filter(b -> b.getStatus() == com.vodovod.model.BillStatus.PENDING || b.getStatus() == com.vodovod.model.BillStatus.PARTIALLY_PAID)
            .map(com.vodovod.model.Bill::getRemainingAmount)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("openBillsCount", openBillsCount);
        model.addAttribute("openBillsTotal", openBillsTotal);

        java.util.Optional<com.vodovod.model.MeterReading> latestReading = userService.getLatestReadingForUser(user);
        latestReading.ifPresent(r -> {
            model.addAttribute("latestReadingValue", r.getReadingValue());
            model.addAttribute("latestReadingDate", r.getReadingDate());
        });
        
        return "account/profile";
    }
}
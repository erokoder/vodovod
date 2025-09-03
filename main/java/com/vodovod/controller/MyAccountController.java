package com.vodovod.controller;

import com.vodovod.model.User;
import com.vodovod.model.Bill;
import com.vodovod.repository.BillRepository;
import com.vodovod.service.BillPdfService;
import com.vodovod.service.SettingsService;
import com.vodovod.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.List;

@Controller
public class MyAccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillPdfService billPdfService;

    @Autowired
    private SettingsService settingsService;

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
        List<Bill> bills = billRepository.findActiveByUser(user);
        model.addAttribute("bills", bills);
        
        return "account/my-bills";
    }

    @GetMapping("/my-bills/{id}/pdf")
    public ResponseEntity<byte[]> downloadMyBillPdf(@PathVariable("id") Long id, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.getUserByUsername(username).orElseThrow();

        Bill bill = billRepository.findById(id).orElseThrow();
        if (!bill.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        byte[] pdf = billPdfService.createBillPdf(bill, settingsService.getCurrentSettingsOrNew());
        String filename = (bill.getBillNumber() != null ? bill.getBillNumber().replace('/', '-') : ("racun-" + id)) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
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
        
        return "account/profile";
    }
}
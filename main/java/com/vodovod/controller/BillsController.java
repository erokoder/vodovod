package com.vodovod.controller;

import com.vodovod.dto.BillPreviewDTO;
import com.vodovod.model.Bill;
import com.vodovod.model.User;
import com.vodovod.repository.BillRepository;
import com.vodovod.service.BillService;
import com.vodovod.service.UserService;
import com.vodovod.service.SettingsService;
import com.vodovod.service.BillPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;

@Controller
@RequestMapping("/bills")
@PreAuthorize("hasRole('ADMIN')")
public class BillsController {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BillService billService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private BillPdfService billPdfService;

    @GetMapping
    public String index(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {
        model.addAttribute("pageTitle", "Računi");
        model.addAttribute("activeMenu", "bills");
        List<User> users = userService.getActiveWaterUsers();
        model.addAttribute("users", users);

        // Selected filter values back to the view
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("selectedFromDate", fromDate);
        model.addAttribute("selectedToDate", toDate);

        User selectedUser = null;
        if (userId != null) {
            Optional<User> user = userService.getUserById(userId);
            if (user.isPresent()) {
                selectedUser = user.get();
            }
        }

        model.addAttribute("bills", billRepository.search(selectedUser, fromDate, toDate));
        return "bills/index";
    }

    @GetMapping("/api/preview")
    @ResponseBody
    public List<BillPreviewDTO> preview(@RequestParam(value = "userId", required = false) Long userId) {
        return billService.previewAllBills(Optional.ofNullable(userId));
    }

    @PostMapping("/api/generate")
    @ResponseBody
    public Map<String, Object> generate(@RequestBody List<BillPreviewDTO> previews, Authentication authentication) {
        List<Bill> saved = billService.generateBillsFromPreview(previews, authentication.getName());
        return Map.of("status", "ok", "count", saved.size());
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable("id") Long id) {
        Bill bill = billRepository.findById(id).orElseThrow();
        byte[] pdf = billPdfService.createBillPdf(bill, settingsService.getCurrentSettingsOrNew());
        String filename = (bill.getBillNumber() != null ? bill.getBillNumber().replace('/', '-') : ("racun-" + id)) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
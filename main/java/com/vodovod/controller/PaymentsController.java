package com.vodovod.controller;

import com.vodovod.model.Bill;
import com.vodovod.model.User;
import com.vodovod.repository.BillRepository;
import com.vodovod.repository.PaymentRepository;
import com.vodovod.service.PaymentService;
import com.vodovod.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/payments")
public class PaymentsController {

    @Autowired
    private UserService userService;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "userId", required = false) Long userId,
                        @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        model.addAttribute("pageTitle", "Uplate");
        model.addAttribute("activeMenu", "payments");
        // Populate users for filter dropdown
        model.addAttribute("users", userService.getActiveWaterUsers());
        model.addAttribute("selectedUserId", userId);
        if (startDate != null || endDate != null || userId != null) {
            // no defaulting; repository handles nulls flexibly
            User selectedUser = null;
            if (userId != null) {
                Optional<User> userOpt = userService.getUserById(userId);
                if (userOpt.isPresent()) {
                    selectedUser = userOpt.get();
                }
            }
            model.addAttribute("payments", paymentRepository.search(selectedUser, startDate, endDate));
        } else {
            model.addAttribute("payments", paymentRepository.findAllOrderByPaymentDateDesc());
        }
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "payments/index";
    }
    
    @GetMapping("/new")
    public String newPayment(Model model) {
        model.addAttribute("pageTitle", "Novo plaćanje");
        model.addAttribute("users", userService.getActiveWaterUsers());
        return "payments/new";
    }

    @GetMapping("/open-bills")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getOpenBills(@RequestParam("userId") Long userId) {
        User user = userService.getUserById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        List<Bill> bills = billRepository.findOpenBillsByUser(user);
        List<Map<String, Object>> result = bills.stream().map(b -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("billNumber", b.getBillNumber());
            m.put("issueDate", b.getIssueDate());
            m.put("total", b.getTotalAmount());
            m.put("paid", b.getPaidAmount());
            m.put("remaining", b.getTotalAmount().subtract(b.getPaidAmount()));
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/new")
    public String postNewPayment(@RequestParam("userId") Long userId,
                                 @RequestParam(value = "billId", required = false) Long billId,
                                 @RequestParam("paymentDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate,
                                 @RequestParam("amount") BigDecimal amount,
                                 @RequestParam("paymentMethod") String paymentMethod) {
        paymentService.recordPayment(userId, billId, paymentDate, amount, paymentMethod, "system");
        return "redirect:/payments";
    }

    @PostMapping("/{id}/cancel")
    public Object cancelPayment(@PathVariable("id") Long id,
                                @RequestParam(value = "reason", required = false) String reason,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        paymentService.cancelPayment(id, "system", reason);

        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        boolean wantsJson = (requestedWith != null && requestedWith.equalsIgnoreCase("XMLHttpRequest"))
                || (accept != null && accept.toLowerCase().contains("application/json"));

        if (wantsJson) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "ok");
            resp.put("paymentId", id);
            return ResponseEntity.ok(resp);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Uplata je uspješno stornirana.");
        return "redirect:/payments";
    }
}
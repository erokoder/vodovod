package com.vodovod.controller;

import com.vodovod.dto.BillPreviewDTO;
import com.vodovod.model.Bill;
import com.vodovod.model.User;
import com.vodovod.repository.BillRepository;
import com.vodovod.service.BillService;
import com.vodovod.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @GetMapping
    public String index(@RequestParam(value = "userId", required = false) Long userId, Model model) {
        model.addAttribute("pageTitle", "Računi");
        model.addAttribute("activeMenu", "bills");
        List<User> users = userService.getActiveWaterUsers();
        model.addAttribute("users", users);

        if (userId != null) {
            Optional<User> user = userService.getUserById(userId);
            if (user.isPresent()) {
                model.addAttribute("bills", billRepository.findByUserOrderByIssueDateDesc(user.get()));
                model.addAttribute("selectedUserId", userId);
            } else {
                model.addAttribute("bills", billRepository.findAll());
            }
        } else {
            model.addAttribute("bills", billRepository.findAll());
        }
        return "bills/index";
    }

    @GetMapping("/api/preview")
    @ResponseBody
    public List<BillPreviewDTO> preview(@RequestParam(value = "userId", required = false) Long userId) {
        return billService.previewAllBills(Optional.ofNullable(userId));
    }

    @PostMapping("/api/generate")
    @ResponseBody
    public List<Bill> generate(@RequestBody List<BillPreviewDTO> previews, Authentication authentication) {
        return billService.generateBillsFromPreview(previews, authentication.getName());
    }
}
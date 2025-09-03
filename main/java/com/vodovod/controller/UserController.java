package com.vodovod.controller;

import com.vodovod.model.Role;
import com.vodovod.model.User;
import com.vodovod.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        
        model.addAttribute("pageTitle", "Korisnici");
        model.addAttribute("activeMenu", "users");
        model.addAttribute("pageActions", java.util.List.of(
                java.util.Map.of(
                        "url", "/users/new",
                        "cssClass", "btn btn-primary btn-sm",
                        "label", "Dodaj Korisnika",
                        "id", "btn-new-user"
                )
        ));
        model.addAttribute("users", users);
        
        return "users/list";
    }

    @GetMapping("/new")
    public String newUserForm(Model model) {
        User user = new User();
        user.setRole(Role.USER); // Default role
        
        model.addAttribute("pageTitle", "Novi Korisnik");
        model.addAttribute("activeMenu", "users");
        model.addAttribute("pageActions", java.util.List.of(
                java.util.Map.of(
                        "url", "/users",
                        "cssClass", "btn btn-outline-secondary btn-sm",
                        "label", "Natrag",
                        "id", "btn-back"
                )
        ));
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        
        return "users/form";
    }

    @PostMapping("/new")
    public String createUser(@Valid @ModelAttribute User user, 
                           BindingResult result, 
                           RedirectAttributes redirectAttributes,
                           Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Novi Korisnik");
            model.addAttribute("activeMenu", "users");
            model.addAttribute("roles", Role.values());
            return "users/form";
        }

        try {
            // Generiraj lozinku ako nije unesena
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                user.setPassword(userService.generateRandomPassword());
            }
            
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Korisnik " + user.getFullName() + " je uspješno kreiran.");
                
            return "redirect:/users";
            
        } catch (Exception e) {
            model.addAttribute("pageTitle", "Novi Korisnik");
            model.addAttribute("activeMenu", "users");
            model.addAttribute("roles", Role.values());
            model.addAttribute("errorMessage", e.getMessage());
            return "users/form";
        }
    }

    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Korisnik nije pronađen.");
            return "redirect:/users";
        }

        model.addAttribute("pageTitle", "Pregled Korisnika");
        model.addAttribute("activeMenu", "users");
        model.addAttribute("pageActions", java.util.List.of(
                java.util.Map.of(
                        "url", "/users/" + id + "/edit",
                        "cssClass", "btn btn-primary btn-sm",
                        "label", "Uredi",
                        "id", "btn-edit-user"
                ),
                java.util.Map.of(
                        "url", "/users",
                        "cssClass", "btn btn-outline-secondary btn-sm",
                        "label", "Povratak",
                        "id", "btn-back"
                )
        ));
        model.addAttribute("user", userOpt.get());
        
        return "users/view";
    }

    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Korisnik nije pronađen.");
            return "redirect:/users";
        }

        model.addAttribute("pageTitle", "Uredi Korisnika");
        model.addAttribute("activeMenu", "users");
        model.addAttribute("pageActions", java.util.List.of(
                java.util.Map.of(
                        "url", "/users",
                        "cssClass", "btn btn-outline-secondary btn-sm",
                        "label", "Povratak",
                        "id", "btn-back"
                )
        ));
        model.addAttribute("user", userOpt.get());
        model.addAttribute("roles", Role.values());
        
        return "users/form";
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
                           @Valid @ModelAttribute User user,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        
        user.setId(id); // Osiguraj da se koristi pravi ID
        
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Uredi Korisnika");
            model.addAttribute("activeMenu", "users");
            model.addAttribute("roles", Role.values());
            return "users/form";
        }

        try {
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Korisnik " + user.getFullName() + " je uspješno ažuriran.");
                
            return "redirect:/users";
            
        } catch (Exception e) {
            model.addAttribute("pageTitle", "Uredi Korisnika");
            model.addAttribute("activeMenu", "users");
            model.addAttribute("roles", Role.values());
            model.addAttribute("errorMessage", e.getMessage());
            return "users/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                userService.deleteUser(id);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Korisnik " + userOpt.get().getFullName() + " je uspješno onemogućen.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Korisnik nije pronađen.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Greška pri brisanju korisnika: " + e.getMessage());
        }
        
        return "redirect:/users";
    }

    @PostMapping("/{id}/enable")
    public String enableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                userService.enableUser(id);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Korisnik " + userOpt.get().getFullName() + " je uspješno omogućen.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Korisnik nije pronađen.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Greška pri omogućavanju korisnika: " + e.getMessage());
        }
        
        return "redirect:/users";
    }
}
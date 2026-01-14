package com.vodovod.controller;

import com.vodovod.model.Organization;
import com.vodovod.model.Role;
import com.vodovod.model.User;
import com.vodovod.repository.OrganizationRepository;
import com.vodovod.repository.UserRepository;
import com.vodovod.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/organizations")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class OrganizationController {

    private final OrganizationRepository organizationRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public OrganizationController(OrganizationRepository organizationRepository, UserService userService, UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "Organizacije");
        model.addAttribute("activeMenu", "organizations");
        List<Organization> organizations = organizationRepository.findAll();
        model.addAttribute("organizations", organizations);

        List<Long> orgIds = organizations.stream()
                .map(Organization::getId)
                .filter(id -> id != null)
                .toList();

        Map<Long, List<User>> activeAdminsByOrgId = new HashMap<>();
        if (!orgIds.isEmpty()) {
            for (User u : userRepository.findAdminsForOrganizations(orgIds)) {
                if (u.getOrganization() == null || u.getOrganization().getId() == null) continue;
                activeAdminsByOrgId.computeIfAbsent(u.getOrganization().getId(), k -> new java.util.ArrayList<>()).add(u);
            }
        }
        model.addAttribute("activeAdminsByOrgId", activeAdminsByOrgId);
        return "organizations/list";
    }

    @GetMapping("/new")
    public String newOrg(Model model) {
        model.addAttribute("pageTitle", "Nova organizacija");
        model.addAttribute("activeMenu", "organizations");
        model.addAttribute("organization", new Organization());
        model.addAttribute("adminUser", new User());
        return "organizations/new";
    }

    @PostMapping("/new")
    public String createOrg(@Valid @ModelAttribute("organization") Organization organization,
                            BindingResult orgResult,
                            @ModelAttribute("adminUser") User adminUser,
                            RedirectAttributes redirectAttributes,
                            Model model) {

        if (orgResult.hasErrors()) {
            model.addAttribute("pageTitle", "Nova organizacija");
            model.addAttribute("activeMenu", "organizations");
            return "organizations/new";
        }

        if (organizationRepository.existsBySlug(organization.getSlug())) {
            orgResult.rejectValue("slug", "error.slug", "Oznaka (slug) već postoji: " + organization.getSlug());
            model.addAttribute("pageTitle", "Nova organizacija");
            model.addAttribute("activeMenu", "organizations");
            return "organizations/new";
        }

        // If admin is being created, validate uniqueness BEFORE creating organization
        if (adminUser.getUsername() != null && !adminUser.getUsername().isBlank()) {
            String normalizedUsername = adminUser.getUsername().trim().toLowerCase();
            adminUser.setUsername(normalizedUsername);
            if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
                model.addAttribute("pageTitle", "Nova organizacija");
                model.addAttribute("activeMenu", "organizations");
                model.addAttribute("errorMessage", "Korisničko ime već postoji: " + normalizedUsername);
                return "organizations/new";
            }
            if (adminUser.getEmail() != null && !adminUser.getEmail().isBlank()) {
                String normalizedEmail = adminUser.getEmail().trim().toLowerCase();
                adminUser.setEmail(normalizedEmail);
                if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                    model.addAttribute("pageTitle", "Nova organizacija");
                    model.addAttribute("activeMenu", "organizations");
                    model.addAttribute("errorMessage", "Email adresa već postoji: " + normalizedEmail);
                    return "organizations/new";
                }
            }
            if (adminUser.getFirstName() == null || adminUser.getFirstName().isBlank()
                    || adminUser.getLastName() == null || adminUser.getLastName().isBlank()) {
                model.addAttribute("pageTitle", "Nova organizacija");
                model.addAttribute("activeMenu", "organizations");
                model.addAttribute("errorMessage", "Za admin korisnika su obavezni Ime i Prezime.");
                return "organizations/new";
            }
        }

        Organization savedOrg = organizationRepository.save(organization);

        // Create an admin for this organization (optional but practical)
        if (adminUser.getUsername() != null && !adminUser.getUsername().isBlank()) {
            adminUser.setRole(Role.ADMIN);
            adminUser.setOrganization(savedOrg);
            userService.createUser(adminUser);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Organizacija je uspješno kreirana.");
        return "redirect:/organizations";
    }
}



package com.vodovod.service;

import com.vodovod.model.Organization;
import com.vodovod.model.Role;
import com.vodovod.model.User;
import com.vodovod.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new RuntimeException("Niste prijavljeni.");
        }
        return userRepository.findByUsernameIgnoreCase(auth.getName().trim())
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen: " + auth.getName()));
    }

    public Organization requireCurrentOrganization() {
        User u = requireCurrentUser();
        if (u.getRole() == Role.SUPER_ADMIN) {
            throw new RuntimeException("SUPER_ADMIN nema organizacijski kontekst.");
        }
        if (u.getOrganization() != null) {
            return u.getOrganization();
        }
        throw new RuntimeException("Organizacija nije konfigurirana za ovog korisnika.");
    }

    public Long requireCurrentOrganizationId() {
        return requireCurrentOrganization().getId();
    }
}



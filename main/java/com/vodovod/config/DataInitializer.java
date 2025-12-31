package com.vodovod.config;

import com.vodovod.model.Role;
import com.vodovod.model.User;
import com.vodovod.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Ensure "admin" exists and is SUPER_ADMIN (upgrade existing DBs)
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setFirstName("Administrator");
            admin.setLastName("Vodovoda");
            admin.setEmail("admin@vodovod.hr");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEnabled(true);
            admin.setRole(Role.SUPER_ADMIN);
            userRepository.save(admin);
            System.out.println("✓ Kreiran super-admin korisnik (username: admin, password: admin123)");
        } else {
            boolean changed = false;
            if (admin.getRole() != Role.SUPER_ADMIN) {
                admin.setRole(Role.SUPER_ADMIN);
                changed = true;
            }
            if (!admin.isEnabled()) {
                admin.setEnabled(true);
                changed = true;
            }
            // Keep existing password (so we don't break existing installs). Only set if missing.
            if (admin.getPassword() == null || admin.getPassword().isBlank()) {
                admin.setPassword(passwordEncoder.encode("admin123"));
                changed = true;
            }
            if (changed) {
                userRepository.save(admin);
                System.out.println("✓ Ažuriran 'admin' u SUPER_ADMIN (username: admin)");
            }
        }
    }
}
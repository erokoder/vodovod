package com.vodovod.config;

import com.vodovod.model.Role;
import com.vodovod.model.SystemSettings;
import com.vodovod.model.User;
import com.vodovod.repository.SystemSettingsRepository;
import com.vodovod.repository.UserRepository;
import com.vodovod.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SystemSettingsRepository systemSettingsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Kreiraj admin korisnika ako ne postoji
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setFirstName("Administrator");
            admin.setLastName("Vodovoda");
            admin.setEmail("admin@vodovod.hr");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            
            userRepository.save(admin);
            System.out.println("✓ Kreiran admin korisnik (username: admin, password: admin123)");
        }

        // Kreiraj sistemske postavke ako ne postoje
        if (systemSettingsRepository.findTopByOrderByIdAsc().isEmpty()) {
            SystemSettings settings = new SystemSettings();
            settings.setWaterPricePerM3(new BigDecimal("0.30"));
            settings.setFixedFee(new BigDecimal("15.00"));
            settings.setUseFixedFee(true);
            settings.setAccountNumber("HR1234567890123456789");
            settings.setCompanyName("Vodovod d.o.o.");
            settings.setCompanyAddress("Glavna ulica 1, 10000 Zagreb");
            settings.setCompanyPhone("01/234-5678");
            settings.setCompanyOib("12345678901");
            settings.setCompanyEmail("info@vodovod.hr");
            settings.setBillDueDays(30);
            settings.setUpdatedBy("admin");
            systemSettingsRepository.save(settings);
            System.out.println("✓ Kreirane početne sistemske postavke");
        }
    }
}
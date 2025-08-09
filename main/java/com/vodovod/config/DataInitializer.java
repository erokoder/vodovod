package com.vodovod.config;

import com.vodovod.model.Role;
import com.vodovod.model.SystemSettings;
import com.vodovod.model.User;
import com.vodovod.repository.SystemSettingsRepository;
import com.vodovod.repository.UserRepository;
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

    @Override
    public void run(String... args) throws Exception {
        // Kreiraj admin korisnika ako ne postoji
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setFirstName("Administrator");
            admin.setLastName("Sistema");
            admin.setEmail("admin@vodovod.hr");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            
            userRepository.save(admin);
            System.out.println("✓ Kreiran admin korisnik (username: admin, password: admin123)");
        }

        // Kreiraj sistemske postavke ako ne postoje
        if (systemSettingsRepository.findFirst().isEmpty()) {
            SystemSettings settings = new SystemSettings();
            settings.setWaterPricePerM3(new BigDecimal("2.50"));
            settings.setFixedFee(new BigDecimal("15.00"));
            settings.setUseFixedFee(true);
            settings.setAccountNumber("HR1234567890123456789");
            settings.setCompanyName("Vodovod d.o.o.");
            settings.setCompanyAddress("Glavna ulica 1, 10000 Zagreb");
            settings.setCompanyPhone("01/234-5678");
            settings.setCompanyEmail("info@vodovod.hr");
            settings.setBillDueDays(30);
            settings.setUpdatedBy("sistem");
            
            systemSettingsRepository.save(settings);
            System.out.println("✓ Kreirane početne sistemske postavke");
        }

        // Kreiraj test korisnika vodovoda ako ga nema
        if (!userRepository.existsByUsername("user1")) {
            User user = new User();
            user.setUsername("user1");
            user.setFirstName("Marko");
            user.setLastName("Marković");
            user.setEmail("marko@example.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(Role.USER);
            user.setMeterNumber("VM001");
            user.setAddress("Test ulica 1, Zagreb");
            user.setPhoneNumber("098/123-456");
            user.setEnabled(true);
            
            userRepository.save(user);
            System.out.println("✓ Kreiran test korisnik (username: user1, password: user123, meter: VM001)");
        }
    }
}
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
            try {
                User admin = new User();
                admin.setUsername("admin");
                admin.setFirstName("Administrator");
                admin.setLastName("Sistema");
                admin.setEmail("admin@vodovod.hr");
                // Postavi plain lozinku; UserService će je ispravno enkriptirati
                admin.setPassword("admin123");
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);

                userService.createUser(admin);
                System.out.println("✓ Kreiran admin korisnik (username: admin, password: admin123)");
            } catch (Exception e) {
                System.err.println("✗ Nije moguće kreirati admin korisnika: " + e.getMessage());
            }
        }

        // Kreiraj sistemske postavke ako ne postoje
        if (systemSettingsRepository.findTopByOrderByIdAsc().isEmpty()) {
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
            // Default cijena električne energije
            settings.setElectricityPricePerKWh(new BigDecimal("0.20"));

            systemSettingsRepository.save(settings);
            System.out.println("✓ Kreirane početne sistemske postavke");
        }

        // Kreiraj test korisnika vodovoda ako ga nema
        if (!userRepository.existsByUsername("user1")) {
            try {
                User user = new User();
                user.setUsername("user1");
                user.setFirstName("Marko");
                user.setLastName("Marković");
                user.setEmail("marko@example.com");
                // Postavi plain lozinku; UserService će je enkriptirati
                user.setPassword("user123");
                user.setRole(Role.USER);
                user.setMeterNumber("VM001");
                user.setAddress("Test ulica 1, Zagreb");
                user.setPhoneNumber("098/123-456");
                user.setEnabled(true);

                userService.createUser(user);
                System.out.println("✓ Kreiran test korisnik (username: user1, password: user123, meter: VM001)");
            } catch (Exception e) {
                System.err.println("✗ Nije moguće kreirati test korisnika: " + e.getMessage());
            }
        }
    }
}
package hr.vodovod.app.config;

import hr.vodovod.app.entity.SystemSettings;
import hr.vodovod.app.entity.User;
import hr.vodovod.app.repository.SystemSettingsRepository;
import hr.vodovod.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final SystemSettingsRepository systemSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Initialize admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Administrator");
            admin.setLastName("Sistema");
            admin.setEmail("admin@vodovod.hr");
            admin.setRole(User.UserRole.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            log.info("Admin korisnik kreiran: username=admin, password=admin123");
        }
        
        // Initialize system settings if not exists
        if (systemSettingsRepository.count() == 0) {
            SystemSettings settings = new SystemSettings();
            settings.setCompanyName("Vodovod d.o.o.");
            settings.setCompanyAddress("Glavna ulica 1");
            settings.setCompanyCity("Zagreb");
            settings.setCompanyPostalCode("10000");
            settings.setCompanyPhone("+385 1 234 5678");
            settings.setCompanyEmail("info@vodovod.hr");
            settings.setCompanyVatNumber("12345678901");
            settings.setBankName("Hrvatska banka");
            settings.setBankAccountNumber("HR1234567890123456789");
            systemSettingsRepository.save(settings);
            log.info("Sistemske postavke inicijalizirane");
        }
    }
}
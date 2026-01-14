package com.vodovod.service;

import com.vodovod.model.Role;
import com.vodovod.model.User;
import com.vodovod.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MeterReadingService meterReadingService;

    @Autowired
    private CurrentUserService currentUserService;

    public List<User> getAllUsers() {
        Long orgId = currentUserService.requireCurrentOrganizationId();
        return userRepository.findByOrganizationId(orgId);
    }

    public List<User> getUsersByRole(Role role) {
        Long orgId = currentUserService.requireCurrentOrganizationId();
        return userRepository.findByOrganizationIdAndRoleAndEnabledTrue(orgId, role);
    }

    public List<User> getActiveWaterUsers() {
        Long orgId = currentUserService.requireCurrentOrganizationId();
        return userRepository.findActiveWaterUsers(orgId);
    }

    public Optional<User> getUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return userOpt;
        User u = userOpt.get();
        if (currentUserService.requireCurrentUser().getRole() == Role.SUPER_ADMIN) {
            return userOpt;
        }
        Long orgId = currentUserService.requireCurrentOrganizationId();
        if (u.getOrganization() == null || !orgId.equals(u.getOrganization().getId())) {
            return Optional.empty();
        }
        return userOpt;
    }

    public Optional<User> findById(Long id) {
        return getUserById(id);
    }

    public List<User> findByRoleAndEnabledTrue(Role role) {
        Long orgId = currentUserService.requireCurrentOrganizationId();
        return userRepository.findByOrganizationIdAndRoleAndEnabledTrue(orgId, role);
    }

    public Optional<User> getUserByUsername(String username) {
        if (username == null) return Optional.empty();
        return userRepository.findByUsernameIgnoreCase(username.trim());
    }

    public Optional<User> getUserByMeterNumber(String meterNumber) {
        Long orgId = currentUserService.requireCurrentOrganizationId();
        return userRepository.findByMeterNumber(orgId, meterNumber);
    }

    public User saveUser(User user) {
        if (user.getUsername() != null) {
            user.setUsername(user.getUsername().trim().toLowerCase());
        }
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim().toLowerCase());
        }
        // Enkriptiraj lozinku ako nije već enkriptirana
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public User createUser(User user) {
        // ADMIN users must never be able to create SUPER_ADMINs (and SUPER_ADMIN is managed outside /users)
        if (user.getRole() == Role.SUPER_ADMIN) {
            throw new RuntimeException("Nije dozvoljeno kreirati SUPER_ADMIN korisnika.");
        }

        if (user.getUsername() != null) {
            user.setUsername(user.getUsername().trim().toLowerCase());
        }
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim().toLowerCase());
        }

        // Force organization to be the current user's organization (unless already set, e.g. org creation flow)
        if (user.getOrganization() == null) {
            user.setOrganization(currentUserService.requireCurrentOrganization());
        }

        // Provjeri jedinstvenost korisničkog imena
        if (userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
            throw new RuntimeException("Korisničko ime već postoji: " + user.getUsername());
        }

        // Provjeri jedinstvenost email adrese
        if (user.getEmail() != null && userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new RuntimeException("Email adresa već postoji: " + user.getEmail());
        }

        // Validacije ovisno o roli
        if (user.getRole() == Role.USER) {
            if (user.getMeterNumber() == null || user.getMeterNumber().trim().isEmpty()) {
                throw new RuntimeException("Broj vodomjera je obavezan za korisnika vodovoda.");
            }
            if (user.getInitialMeterReadingValue() == null) {
                throw new RuntimeException("Početno stanje vodomjera je obavezno.");
            }
            if (user.getInitialMeterReadingValue().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Početno stanje vodomjera mora biti >= 0.");
            }
            if (user.getInitialMeterReadingDate() == null) {
                throw new RuntimeException("Datum početnog stanja je obavezan.");
            }
        } else if (user.getRole() == Role.ADMIN) {
            // Administrator ne smije imati broj vodomjera
            user.setMeterNumber(null);
            user.setInitialMeterReadingValue(null);
            user.setInitialMeterReadingDate(null);
        }

        // Provjeri jedinstvenost broja vodomjera
        if (user.getMeterNumber() != null && !user.getMeterNumber().trim().isEmpty()) {
            if (userRepository.findByMeterNumber(user.getOrganization().getId(), user.getMeterNumber()).isPresent()) {
                throw new RuntimeException("Broj vodomjera već postoji: " + user.getMeterNumber());
            }
        }

        User savedUser = saveUser(user);

        // Ako je korisnik tipa USER i ima broj vodomjera, kreiraj početno očitanje iz forme
        if (savedUser.getRole() == Role.USER
                && savedUser.getMeterNumber() != null
                && !savedUser.getMeterNumber().trim().isEmpty()) {
            LocalDate readingDate = user.getInitialMeterReadingDate();
            BigDecimal readingValue = user.getInitialMeterReadingValue();
            com.vodovod.model.MeterReading initialReading = new com.vodovod.model.MeterReading(
                    savedUser,
                    readingDate,
                    readingValue
            );
            initialReading.setNotes("Početno očitanje");
            initialReading.setCreatedBy("sistem");
            meterReadingService.saveInitialReadingForNewUser(initialReading);
        }

        return savedUser;
    }

    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        // Tenant guard: only allow edits within current org (SUPER_ADMIN bypass)
        if (currentUserService.requireCurrentUser().getRole() != Role.SUPER_ADMIN) {
            Long orgId = currentUserService.requireCurrentOrganizationId();
            if (existingUser.getOrganization() == null || !orgId.equals(existingUser.getOrganization().getId())) {
                throw new RuntimeException("Nemate pristup ovom korisniku.");
            }
            user.setOrganization(existingUser.getOrganization());
        } else if (user.getOrganization() == null) {
            user.setOrganization(existingUser.getOrganization());
        }

        // Onemogući promjenu role nakon kreiranja
        if (user.getRole() != existingUser.getRole()) {
            throw new RuntimeException("Uloga se ne može mijenjati nakon kreiranja korisnika.");
        }

        // Provjeri jedinstvenost korisničkog imena (osim za trenutnog korisnika)
        if (user.getUsername() != null) {
            user.setUsername(user.getUsername().trim().toLowerCase());
        }
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim().toLowerCase());
        }

        Optional<User> userWithSameUsername = userRepository.findByUsernameIgnoreCase(user.getUsername());
        if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(user.getId())) {
            throw new RuntimeException("Korisničko ime već postoji: " + user.getUsername());
        }

        // Provjeri jedinstvenost email adrese (osim za trenutnog korisnika)
        if (user.getEmail() != null) {
            Optional<User> userWithSameEmail = userRepository.findByEmailIgnoreCase(user.getEmail());
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(user.getId())) {
                throw new RuntimeException("Email adresa već postoji: " + user.getEmail());
            }
        }

        // Validacije ovisno o roli i namještanje broja vodomjera
        if (existingUser.getRole() == Role.USER) {
            if (user.getMeterNumber() == null || user.getMeterNumber().trim().isEmpty()) {
                throw new RuntimeException("Broj vodomjera je obavezan za korisnika vodovoda.");
            }
        } else if (existingUser.getRole() == Role.ADMIN) {
            user.setMeterNumber(null);
        }

        // Provjeri jedinstvenost broja vodomjera (osim za trenutnog korisnika)
        if (user.getMeterNumber() != null && !user.getMeterNumber().trim().isEmpty()) {
            Long orgId = user.getOrganization() != null ? user.getOrganization().getId() : currentUserService.requireCurrentOrganizationId();
            Optional<User> userWithSameMeter = userRepository.findByMeterNumber(orgId, user.getMeterNumber());
            if (userWithSameMeter.isPresent() && !userWithSameMeter.get().getId().equals(user.getId())) {
                throw new RuntimeException("Broj vodomjera već postoji: " + user.getMeterNumber());
            }
        }

        // Ako lozinka nije promijenjena, zadrži staru
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            user.setPassword(existingUser.getPassword());
        }

        // Zadrži datum kreiranja
        user.setCreatedAt(existingUser.getCreatedAt());

        // Spriječi uklanjanje administratorske uloge ili onemogućavanje jedinog administratora
        if (existingUser.getRole() == Role.ADMIN) {
            long enabledAdmins = userRepository.countByOrganizationAndRoleAndEnabled(existingUser.getOrganization().getId(), Role.ADMIN);
            boolean roleChangedFromAdminToNonAdmin = user.getRole() != null && user.getRole() != Role.ADMIN;
            boolean willBeDisabled = !user.isEnabled();
            if ((roleChangedFromAdminToNonAdmin || willBeDisabled) && enabledAdmins <= 1) {
                throw new RuntimeException("Nije moguće ukloniti administratorsku ulogu ili onemogućiti jedinog administratora u sistemu.");
            }
        }

        return saveUser(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        if (currentUserService.requireCurrentUser().getRole() != Role.SUPER_ADMIN) {
            Long orgId = currentUserService.requireCurrentOrganizationId();
            if (user.getOrganization() == null || !orgId.equals(user.getOrganization().getId())) {
                throw new RuntimeException("Nemate pristup ovom korisniku.");
            }
        }
        
        // Onemogući korisnika umjesto brisanja (soft delete)
        // Spriječi onemogućavanje jedinog administratora
        if (user.getRole() == Role.ADMIN && user.isEnabled()) {
            long enabledAdmins = userRepository.countByOrganizationAndRoleAndEnabled(user.getOrganization().getId(), Role.ADMIN);
            if (enabledAdmins <= 1) {
                throw new RuntimeException("Nije moguće onemogućiti jedinog administratora u sistemu.");
            }
        }

        user.setEnabled(false);
        userRepository.save(user);
    }

    public void enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));
        if (currentUserService.requireCurrentUser().getRole() != Role.SUPER_ADMIN) {
            Long orgId = currentUserService.requireCurrentOrganizationId();
            if (user.getOrganization() == null || !orgId.equals(user.getOrganization().getId())) {
                throw new RuntimeException("Nemate pristup ovom korisniku.");
            }
        }
        user.setEnabled(true);
        userRepository.save(user);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public String generateRandomPassword() {
        // Generiraj jednostavnu random lozinku
        return "user" + System.currentTimeMillis() % 10000;
    }
}
package com.vodovod.service;

import com.vodovod.model.Role;
import com.vodovod.model.User;
import com.vodovod.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRoleAndEnabledTrue(role);
    }

    public List<User> getActiveWaterUsers() {
        return userRepository.findActiveWaterUsers();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByMeterNumber(String meterNumber) {
        return userRepository.findByMeterNumber(meterNumber);
    }

    public User saveUser(User user) {
        // Enkriptiraj lozinku ako nije već enkriptirana
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public User createUser(User user) {
        // Provjeri jedinstvenost korisničkog imena
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Korisničko ime već postoji: " + user.getUsername());
        }

        // Provjeri jedinstvenost email adrese
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email adresa već postoji: " + user.getEmail());
        }

        // Provjeri jedinstvenost broja vodomjera
        if (user.getMeterNumber() != null && !user.getMeterNumber().trim().isEmpty()) {
            if (userRepository.findByMeterNumber(user.getMeterNumber()).isPresent()) {
                throw new RuntimeException("Broj vodomjera već postoji: " + user.getMeterNumber());
            }
        }

        return saveUser(user);
    }

    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        // Provjeri jedinstvenost korisničkog imena (osim za trenutnog korisnika)
        Optional<User> userWithSameUsername = userRepository.findByUsername(user.getUsername());
        if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(user.getId())) {
            throw new RuntimeException("Korisničko ime već postoji: " + user.getUsername());
        }

        // Provjeri jedinstvenost email adrese (osim za trenutnog korisnika)
        if (user.getEmail() != null) {
            Optional<User> userWithSameEmail = userRepository.findByEmail(user.getEmail());
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(user.getId())) {
                throw new RuntimeException("Email adresa već postoji: " + user.getEmail());
            }
        }

        // Provjeri jedinstvenost broja vodomjera (osim za trenutnog korisnika)
        if (user.getMeterNumber() != null && !user.getMeterNumber().trim().isEmpty()) {
            Optional<User> userWithSameMeter = userRepository.findByMeterNumber(user.getMeterNumber());
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

        return saveUser(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));
        
        // Onemogući korisnika umjesto brisanja (soft delete)
        user.setEnabled(false);
        userRepository.save(user);
    }

    public void enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));
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
package com.vodovod.service;

import com.vodovod.model.MeterReading;
import com.vodovod.model.Role;
import com.vodovod.model.User;
import com.vodovod.repository.MeterReadingRepository;
import com.vodovod.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MeterReadingService {
    
    @Autowired
    private MeterReadingRepository meterReadingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Dohvaća sve aktivne korisnike sa vodomjerom
     */
    public List<User> getActiveWaterUsers() {
        return userRepository.findActiveWaterUsers();
    }
    
    /**
     * Dohvaća korisnika po ID-ju
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * Dohvaća posljednje očitanje za korisnika
     */
    public Optional<MeterReading> getLatestReadingForUser(User user) {
        return meterReadingRepository.findLatestByUser(user);
    }
    
    /**
     * Validira novo očitanje
     * - Provjerava da li je novo očitanje veće od prethodnog
     * - Provjerava da li već postoji očitanje za isti datum
     */
    public ValidationResult validateNewReading(User user, LocalDate readingDate, BigDecimal newReadingValue) {
        ValidationResult result = new ValidationResult();
        
        // Provjeri da li već postoji očitanje za isti datum
        Optional<MeterReading> existingReading = meterReadingRepository.findByUserAndReadingDate(user, readingDate);
        if (existingReading.isPresent()) {
            result.addError("Već postoji očitanje za ovaj datum.");
            return result;
        }
        
        // Dohvati posljednje očitanje
        Optional<MeterReading> lastReading = getLatestReadingForUser(user);
        if (lastReading.isPresent()) {
            MeterReading previous = lastReading.get();
            
            // Provjeri da li je novo očitanje veće od prethodnog
            if (newReadingValue.compareTo(previous.getReadingValue()) <= 0) {
                result.addError(String.format("Novo očitanje (%s m³) mora biti veće od prethodnog očitanja (%s m³).", 
                    newReadingValue, previous.getReadingValue()));
            }
            
            // Provjeri da li je datum novog očitanja nakon datuma prethodnog
            if (!readingDate.isAfter(previous.getReadingDate())) {
                result.addError(String.format("Datum novog očitanja mora biti nakon datuma prethodnog očitanja (%s).", 
                    previous.getReadingDate()));
            }
            
            result.setPreviousReading(previous);
        }
        
        result.setValid(result.getErrors().isEmpty());
        return result;
    }
    
    /**
     * Sprema novo očitanje
     */
    public MeterReading saveReading(User user, LocalDate readingDate, BigDecimal readingValue, String notes) {
        MeterReading reading = new MeterReading();
        reading.setUser(user);
        reading.setReadingDate(readingDate);
        reading.setReadingValue(readingValue);
        reading.setNotes(notes);
        
        // Postavi prethodno očitanje ako postoji
        Optional<MeterReading> lastReading = getLatestReadingForUser(user);
        if (lastReading.isPresent()) {
            reading.setPreviousReadingValue(lastReading.get().getReadingValue());
            reading.calculateConsumption();
        }
        
        return meterReadingRepository.save(reading);
    }
    
    /**
     * Dohvaća sva očitanja
     */
    public List<MeterReading> getAllReadings() {
        return meterReadingRepository.findAll();
    }
    
    /**
     * Dohvaća očitanja za korisnika
     */
    public List<MeterReading> getReadingsByUser(User user) {
        return meterReadingRepository.findByUserOrderByReadingDateDesc(user);
    }
    
    /**
     * Klasa za rezultat validacije
     */
    public static class ValidationResult {
        private boolean valid = true;
        private List<String> errors = new java.util.ArrayList<>();
        private MeterReading previousReading;
        
        public boolean isValid() {
            return valid;
        }
        
        public void setValid(boolean valid) {
            this.valid = valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public void addError(String error) {
            this.errors.add(error);
            this.valid = false;
        }
        
        public MeterReading getPreviousReading() {
            return previousReading;
        }
        
        public void setPreviousReading(MeterReading previousReading) {
            this.previousReading = previousReading;
        }
        
        public String getErrorMessage() {
            return String.join(" ", errors);
        }
    }
}
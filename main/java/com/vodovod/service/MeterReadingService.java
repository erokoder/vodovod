package com.vodovod.service;

import com.vodovod.model.MeterReading;
import com.vodovod.model.User;
import com.vodovod.repository.MeterReadingRepository;
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

    /**
     * Sprema novo očitanje vodomjera
     */
    public MeterReading saveReading(MeterReading reading) {
        // Pronađi prethodno očitanje za korisnika
        Optional<MeterReading> previousReading = getLatestReadingByUser(reading.getUser());
        
        if (previousReading.isPresent()) {
            reading.setPreviousReadingValue(previousReading.get().getReadingValue());
        } else {
            reading.setPreviousReadingValue(BigDecimal.ZERO);
        }
        
        // Izračunaj potrošnju
        reading.calculateConsumption();
        
        return meterReadingRepository.save(reading);
    }

    /**
     * Dohvaća najnovije očitanje za korisnika
     */
    public Optional<MeterReading> getLatestReadingByUser(User user) {
        return meterReadingRepository.findLatestByUser(user);
    }

    /**
     * Dohvaća sva očitanja za korisnika sortirana po datumu
     */
    public List<MeterReading> getReadingsByUser(User user) {
        return meterReadingRepository.findByUserOrderByReadingDateDesc(user);
    }

    /**
     * Provjerava postoji li već očitanje za korisnika na određeni datum
     */
    public boolean existsReadingForUserAndDate(User user, LocalDate date) {
        return meterReadingRepository.findByUserAndReadingDate(user, date).isPresent();
    }

    /**
     * Validira novo očitanje
     */
    public boolean isValidNewReading(User user, BigDecimal newReading, LocalDate readingDate) {
        // Provjeri postoji li već očitanje za taj datum
        if (existsReadingForUserAndDate(user, readingDate)) {
            return false;
        }

        // Provjeri je li novo očitanje veće od prethodnog
        Optional<MeterReading> latestReading = getLatestReadingByUser(user);
        if (latestReading.isPresent()) {
            BigDecimal latestValue = latestReading.get().getReadingValue();
            if (newReading.compareTo(latestValue) <= 0) {
                return false;
            }
            
            // Provjeri je li datum noviji od prethodnog očitanja
            LocalDate latestDate = latestReading.get().getReadingDate();
            if (!readingDate.isAfter(latestDate)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Dohvaća sva očitanja sortirana po datumu
     */
    public List<MeterReading> getAllReadings() {
        return meterReadingRepository.findAll();
    }

    /**
     * Dohvaća očitanja bez generiranih računa
     */
    public List<MeterReading> getReadingsWithoutBill() {
        return meterReadingRepository.findReadingsWithoutBill();
    }
}
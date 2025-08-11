package com.vodovod.service;

import com.vodovod.model.MeterReading;
import com.vodovod.model.User;
import com.vodovod.repository.MeterReadingRepository;
import com.vodovod.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
public class MeterReadingService {

    @Autowired
    private MeterReadingRepository meterReadingRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Kreira novo očitanje uz potrebne validacije.
     *
     * @param userId       ID korisnika za kojeg se unosi očitanje
     * @param readingDate  Datum očitanja
     * @param readingValue Vrijednost očitanja (novo stanje)
     * @param notes        Napomene (opcionalno)
     * @return Spremljeno očitanje
     */
    public MeterReading createReading(Long userId,
                                      LocalDate readingDate,
                                      BigDecimal readingValue,
                                      String notes) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        // Provjeri da li već postoji očitanje za isti datum i korisnika
        if (meterReadingRepository.findByUserAndReadingDate(user, readingDate).isPresent()) {
            throw new RuntimeException("Očitanje za odabrani datum već postoji.");
        }

        Optional<MeterReading> latestOpt = meterReadingRepository.findLatestByUser(user);
        BigDecimal previousValue = null;
        BigDecimal consumption = null;

        if (latestOpt.isPresent()) {
            previousValue = latestOpt.get().getReadingValue();

            // Novo očitanje mora biti veće od prethodnog
            if (readingValue.compareTo(previousValue) <= 0) {
                throw new RuntimeException("Novo očitanje mora biti veće od prethodnog očitanja (" + previousValue + ").");
            }

            consumption = readingValue.subtract(previousValue);
        }

        MeterReading meterReading = new MeterReading();
        meterReading.setUser(user);
        meterReading.setReadingDate(readingDate);
        meterReading.setReadingValue(readingValue);
        meterReading.setPreviousReadingValue(previousValue);
        meterReading.setConsumption(consumption);
        meterReading.setNotes(notes);

        return meterReadingRepository.save(meterReading);
    }

    /**
     * Vraća posljednje očitanje za korisnika (ako postoji)
     */
    public Optional<MeterReading> getLatestReadingByUser(Long userId) {
        return userRepository.findById(userId)
                .flatMap(meterReadingRepository::findLatestByUser);
    }
}
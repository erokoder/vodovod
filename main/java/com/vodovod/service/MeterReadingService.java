package com.vodovod.service;

import com.vodovod.model.MeterReading;
import com.vodovod.model.User;
import com.vodovod.repository.MeterReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
        return meterReadingRepository.findTopByUserOrderByReadingDateDesc(user);
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
     * Dohvaća sva očitanja sortirana po korisniku (ime) pa po datumu (najnovija prva)
     */
    public List<MeterReading> getAllReadings() {
        return meterReadingRepository.findAllOrderByUserNameAndDateDesc();
    }

    /**
     * Dohvaća očitanja bez generiranih računa
     */
    public List<MeterReading> getReadingsWithoutBill() {
        return meterReadingRepository.findReadingsWithoutBill();
    }

    /**
     * Dohvaća očitanje po ID-u
     */
    public Optional<MeterReading> findById(Long id) {
        return meterReadingRepository.findById(id);
    }

    public List<MeterReading> getAllReadingsByDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null) {
            return meterReadingRepository.findByReadingDateBetween(fromDate, toDate);
        } else if (fromDate != null) {
            return meterReadingRepository.findByReadingDateGreaterThanEqualOrderByReadingDateDesc(fromDate);
        } else if (toDate != null) {
            return meterReadingRepository.findByReadingDateLessThanEqualOrderByReadingDateDesc(toDate);
        } else {
            return getAllReadings();
        }
    }

    public List<MeterReading> getReadingsByUserAndDateRange(User user, LocalDate fromDate, LocalDate toDate) {
        if (user == null) {
            return getAllReadingsByDateRange(fromDate, toDate);
        }
        if (fromDate != null && toDate != null) {
            return meterReadingRepository.findByUserAndReadingDateBetweenOrderByReadingDateDesc(user, fromDate, toDate);
        } else if (fromDate != null) {
            return meterReadingRepository.findByUserAndReadingDateGreaterThanEqualOrderByReadingDateDesc(user, fromDate);
        } else if (toDate != null) {
            return meterReadingRepository.findByUserAndReadingDateLessThanEqualOrderByReadingDateDesc(user, toDate);
        } else {
            return getReadingsByUser(user);
        }
    }

    /**
     * Storno očitanja: potpuno uklanja očitanje i rekalkulira lanac narednih očitanja
     * tako da stanje bude kao da očitanje nikada nije postojalo.
     */
    public void stornoReading(Long readingId, String cancelledBy) {
        MeterReading reading = meterReadingRepository.findById(readingId)
                .orElseThrow(() -> new IllegalArgumentException("Očitanje nije pronađeno"));

        User user = reading.getUser();

        // Dohvati prethodno očitanje (po datumu) kako bismo ga koristili kao nova polazna osnova
        Optional<MeterReading> previousOpt = meterReadingRepository
                .findTopByUserAndReadingDateBeforeOrderByReadingDateDesc(user, reading.getReadingDate());

        BigDecimal newBase = previousOpt.map(MeterReading::getReadingValue).orElse(BigDecimal.ZERO);

        // Dohvati sva naredna očitanja i rekalkuliraj im previous/consumption
        List<MeterReading> subsequent = meterReadingRepository
                .findByUserAndReadingDateAfterOrderByReadingDateAsc(user, reading.getReadingDate());

        BigDecimal lastBase = newBase;
        for (MeterReading mr : subsequent) {
            mr.setPreviousReadingValue(lastBase);
            mr.calculateConsumption();
            // Ukloni oznaku generiranog računa jer se lanac promijenio (oprezan pristup)
            if (mr.isBillGenerated()) {
                mr.setBillGenerated(false);
            }
            lastBase = mr.getReadingValue();
            meterReadingRepository.save(mr);
        }

        // Na kraju izbriši ciljano očitanje
        meterReadingRepository.delete(reading);
    }
}
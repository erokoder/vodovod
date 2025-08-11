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

    @Autowired
    private DashboardService dashboardService;

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
        
        // Spremi očitanje
        MeterReading savedReading = meterReadingRepository.save(reading);
        
        // Osvježi dashboard statistike
        dashboardService.refreshDashboardStats();
        
        return savedReading;
    }

    /**
     * Dohvaća najnovije očitanje za korisnika
     */
    public Optional<MeterReading> getLatestReadingByUser(User user) {
        Optional<MeterReading> reading = meterReadingRepository.findLatestByUser(user);
        
        // Osvježi dashboard statistike
        dashboardService.refreshDashboardStats();
        
        return reading;
    }

    /**
     * Dohvaća sva očitanja za korisnika sortirana po datumu
     */
    public List<MeterReading> getReadingsByUser(User user) {
        List<MeterReading> readings = meterReadingRepository.findByUserOrderByReadingDateDesc(user);
        
        // Osvježi dashboard statistike
        dashboardService.refreshDashboardStats();
        
        return readings;
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
        List<MeterReading> readings = meterReadingRepository.findAll();
        
        // Osvježi dashboard statistike
        dashboardService.refreshDashboardStats();
        
        return readings;
    }

    /**
     * Dohvaća sva očitanja s potrošnjom sortirana po datumu
     */
    public List<MeterReading> getAllReadingsWithConsumption() {
        List<MeterReading> readings = meterReadingRepository.findAllByOrderByReadingDateDesc();
        
        // Osiguraj da je potrošnja izračunata za sva očitanja
        for (MeterReading reading : readings) {
            if (reading.getConsumption() == null) {
                reading.calculateConsumption();
            }
        }
        
        // Osvježi dashboard statistike
        dashboardService.refreshDashboardStats();
        
        return readings;
    }

    /**
     * Dohvaća očitanja bez generiranih računa
     */
    public List<MeterReading> getReadingsWithoutBill() {
        List<MeterReading> readings = meterReadingRepository.findReadingsWithoutBill();
        
        // Osvježi dashboard statistike
        dashboardService.refreshDashboardStats();
        
        return readings;
    }

    /**
     * Dohvaća očitanja za određeni period
     */
    public List<MeterReading> findByReadingDateBetween(LocalDate startDate, LocalDate endDate) {
        List<MeterReading> readings = meterReadingRepository.findByReadingDateBetween(startDate, endDate);
        
        // Osvježi dashboard statistike
        dashboardService.refreshDashboardStats();
        
        return readings;
    }
}
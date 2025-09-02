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
     * Dohvaća najnovije ne-stornirano očitanje za korisnika
     */
    public Optional<MeterReading> getLatestReadingByUser(User user) {
        return meterReadingRepository.findTopByUserAndCancelledFalseOrderByReadingDateDesc(user);
    }

    /**
     * Dohvaća sva ne-stornirana očitanja za korisnika sortirana po datumu
     */
    public List<MeterReading> getReadingsByUser(User user) {
        return meterReadingRepository.findByUserAndCancelledFalseOrderByReadingDateDesc(user);
    }

    /**
     * Provjerava postoji li već ne-stornirano očitanje za korisnika na određeni datum
     */
    public boolean existsReadingForUserAndDate(User user, LocalDate date) {
        return meterReadingRepository.findByUserAndReadingDateAndCancelledFalse(user, date).isPresent();
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
     * Dohvaća sva ne-stornirana očitanja sortirana po korisniku (ime) pa po datumu (najnovija prva)
     */
    public List<MeterReading> getAllReadings() {
        return meterReadingRepository.findAllNonCancelledOrderByUserNameAndDateDesc();
    }

    /**
     * Dohvaća ne-stornirana očitanja bez generiranih računa
     */
    public List<MeterReading> getReadingsWithoutBill() {
        return meterReadingRepository.findNonCancelledReadingsWithoutBill();
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
     * Stornira očitanje i ažurira sva sljedeća očitanja
     */
    @Transactional
    public void cancelReading(Long readingId, String cancelledBy, String reason) {
        // Pronađi očitanje koje se stornira
        MeterReading readingToCancel = meterReadingRepository.findById(readingId)
                .orElseThrow(() -> new RuntimeException("Očitanje nije pronađeno"));

        // Provjeri da li je već stornirano
        if (readingToCancel.isCancelled()) {
            throw new RuntimeException("Očitanje je već stornirano");
        }

        // Provjeri da li je generiran račun za ovo očitanje
        if (readingToCancel.isBillGenerated()) {
            throw new RuntimeException("Ne možete stornirati očitanje za koje je već generiran račun");
        }

        // Storniraj očitanje
        readingToCancel.cancel(cancelledBy, reason);
        meterReadingRepository.save(readingToCancel);

        // Pronađi sva sljedeća očitanja za istog korisnika
        List<MeterReading> subsequentReadings = meterReadingRepository
                .findSubsequentReadingsByUser(readingToCancel.getUser(), readingToCancel.getReadingDate());

        // Ažuriraj prethodne vrijednosti za sva sljedeća očitanja
        updateSubsequentReadings(subsequentReadings, readingToCancel.getUser());
    }

    /**
     * Ažurira prethodne vrijednosti za sljedeća očitanja nakon storniranja
     */
    private void updateSubsequentReadings(List<MeterReading> subsequentReadings, User user) {
        if (subsequentReadings.isEmpty()) {
            return;
        }

        // Prvo očitanje u listi treba da se ažurira sa prethodnim ne-storniranim očitanjem
        MeterReading firstSubsequent = subsequentReadings.get(0);
        
        // Pronađi prethodno ne-stornirano očitanje prije prvog sljedećeg
        Optional<MeterReading> previousValidReading = findPreviousValidReading(user, firstSubsequent.getReadingDate());
        
        BigDecimal previousValue = previousValidReading.isPresent() 
                ? previousValidReading.get().getReadingValue() 
                : BigDecimal.ZERO;

        // Ažuriraj prvo sljedeće očitanje
        firstSubsequent.setPreviousReadingValue(previousValue);
        firstSubsequent.calculateConsumption();
        meterReadingRepository.save(firstSubsequent);

        // Ažuriraj ostala sljedeća očitanja u nizu
        for (int i = 1; i < subsequentReadings.size(); i++) {
            MeterReading current = subsequentReadings.get(i);
            MeterReading previous = subsequentReadings.get(i - 1);
            
            current.setPreviousReadingValue(previous.getReadingValue());
            current.calculateConsumption();
            meterReadingRepository.save(current);
        }
    }

    /**
     * Pronalazi prethodno validno (ne-stornirano) očitanje prije datog datuma
     */
    private Optional<MeterReading> findPreviousValidReading(User user, LocalDate beforeDate) {
        List<MeterReading> allReadings = meterReadingRepository.findByUserAndCancelledFalseOrderByReadingDateDesc(user);
        return allReadings.stream()
                .filter(reading -> reading.getReadingDate().isBefore(beforeDate))
                .findFirst();
    }

    /**
     * Provjeri da li se očitanje može stornirati
     */
    public boolean canCancelReading(Long readingId) {
        Optional<MeterReading> readingOpt = meterReadingRepository.findById(readingId);
        if (readingOpt.isEmpty()) {
            return false;
        }
        
        MeterReading reading = readingOpt.get();
        return !reading.isCancelled() && !reading.isBillGenerated();
    }

    /**
     * Dohvaća sva očitanja (uključujući stornirana) sortirana po korisniku i datumu
     * Korisiti se za admin pregled
     */
    public List<MeterReading> getAllReadingsIncludingCancelled() {
        return meterReadingRepository.findAllOrderByUserNameAndDateDesc();
    }

    /**
     * Dohvaća sva očitanja za korisnika (uključujući stornirana) sortirana po datumu
     * Korisiti se za admin pregled
     */
    public List<MeterReading> getReadingsByUserIncludingCancelled(User user) {
        return meterReadingRepository.findByUserOrderByReadingDateDesc(user);
    }
}
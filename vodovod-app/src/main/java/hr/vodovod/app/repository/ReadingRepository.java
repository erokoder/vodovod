package hr.vodovod.app.repository;

import hr.vodovod.app.entity.Reading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingRepository extends JpaRepository<Reading, Long> {
    
    List<Reading> findByUserIdOrderByReadingDateDesc(Long userId);
    
    @Query("SELECT r FROM Reading r WHERE r.user.id = :userId ORDER BY r.readingDate DESC LIMIT 1")
    Optional<Reading> findLatestByUserId(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Reading r WHERE r.user.id = :userId AND r.invoiceGenerated = false ORDER BY r.readingDate")
    List<Reading> findUninvoicedReadingsByUserId(@Param("userId") Long userId);
    
    List<Reading> findByReadingDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COUNT(r) FROM Reading r WHERE r.invoiceGenerated = false")
    long countUninvoicedReadings();
    
    boolean existsByUserIdAndReadingDate(Long userId, LocalDate readingDate);
}
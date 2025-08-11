package com.vodovod.repository;

import com.vodovod.model.MeterReading;
import com.vodovod.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {
    
    List<MeterReading> findByUserOrderByReadingDateDesc(User user);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.user = :user ORDER BY mr.readingDate DESC")
    List<MeterReading> findByUserOrderByReadingDateDescending(User user);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.user = :user AND mr.readingDate = (SELECT MAX(mr2.readingDate) FROM MeterReading mr2 WHERE mr2.user = :user)")
    Optional<MeterReading> findLatestByUser(User user);
    
    Optional<MeterReading> findTopByUserOrderByReadingDateDesc(User user);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.user = :user AND mr.readingDate = :readingDate")
    Optional<MeterReading> findByUserAndReadingDate(User user, LocalDate readingDate);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.billGenerated = false")
    List<MeterReading> findReadingsWithoutBill();
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.user = :user AND mr.billGenerated = false ORDER BY mr.readingDate ASC")
    List<MeterReading> findUnbilledReadingsByUser(User user);
    
    @Query("SELECT COUNT(mr) FROM MeterReading mr WHERE mr.readingDate BETWEEN :startDate AND :endDate")
    long countByReadingDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.readingDate BETWEEN :startDate AND :endDate ORDER BY mr.readingDate DESC")
    List<MeterReading> findByReadingDateBetween(LocalDate startDate, LocalDate endDate);
}
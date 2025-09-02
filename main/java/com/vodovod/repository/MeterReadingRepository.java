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

    @Query("SELECT mr FROM MeterReading mr JOIN mr.user u ORDER BY u.lastName ASC, u.firstName ASC, mr.readingDate DESC")
    List<MeterReading> findAllOrderByUserNameAndDateDesc();

    // Date range filtering per user
    List<MeterReading> findByUserAndReadingDateBetweenOrderByReadingDateDesc(User user, LocalDate startDate, LocalDate endDate);
    List<MeterReading> findByUserAndReadingDateGreaterThanEqualOrderByReadingDateDesc(User user, LocalDate startDate);
    List<MeterReading> findByUserAndReadingDateLessThanEqualOrderByReadingDateDesc(User user, LocalDate endDate);

    // Date range filtering across all users
    List<MeterReading> findByReadingDateGreaterThanEqualOrderByReadingDateDesc(LocalDate startDate);
    List<MeterReading> findByReadingDateLessThanEqualOrderByReadingDateDesc(LocalDate endDate);

    // Non-cancelled readings queries
    List<MeterReading> findByUserAndCancelledFalseOrderByReadingDateDesc(User user);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.user = :user AND mr.cancelled = false ORDER BY mr.readingDate DESC")
    Optional<MeterReading> findTopByUserAndCancelledFalseOrderByReadingDateDesc(User user);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.user = :user AND mr.readingDate = :readingDate AND mr.cancelled = false")
    Optional<MeterReading> findByUserAndReadingDateAndCancelledFalse(User user, LocalDate readingDate);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.billGenerated = false AND mr.cancelled = false")
    List<MeterReading> findNonCancelledReadingsWithoutBill();
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.user = :user AND mr.billGenerated = false AND mr.cancelled = false ORDER BY mr.readingDate ASC")
    List<MeterReading> findUnbilledNonCancelledReadingsByUser(User user);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.cancelled = false ORDER BY mr.user.lastName ASC, mr.user.firstName ASC, mr.readingDate DESC")
    List<MeterReading> findAllNonCancelledOrderByUserNameAndDateDesc();
    
    // Find readings that come after a specific reading for the same user
    @Query("SELECT mr FROM MeterReading mr WHERE mr.user = :user AND mr.readingDate > :readingDate AND mr.cancelled = false ORDER BY mr.readingDate ASC")
    List<MeterReading> findSubsequentReadingsByUser(User user, LocalDate readingDate);
}
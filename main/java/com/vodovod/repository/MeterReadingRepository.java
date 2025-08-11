package com.vodovod.repository;

import com.vodovod.model.MeterReading;
import com.vodovod.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {
    
    List<MeterReading> findByUserOrderByReadingDateDesc(User user);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.user = :user ORDER BY mr.readingDate DESC")
    List<MeterReading> findByUserOrderByReadingDateDescending(@Param("user") User user);
    
    @Query(value = "SELECT mr FROM MeterReading mr WHERE mr.user = :user ORDER BY mr.readingDate DESC, mr.id DESC")
    List<MeterReading> findLatestByUserQuery(@Param("user") User user);
    
    default Optional<MeterReading> findLatestByUser(User user) {
        List<MeterReading> readings = findLatestByUserQuery(user);
        return readings.isEmpty() ? Optional.empty() : Optional.of(readings.get(0));
    }
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.user = :user AND mr.readingDate = :readingDate")
    Optional<MeterReading> findByUserAndReadingDate(@Param("user") User user, @Param("readingDate") LocalDate readingDate);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.billGenerated = false")
    List<MeterReading> findReadingsWithoutBill();
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.user = :user AND mr.billGenerated = false ORDER BY mr.readingDate ASC")
    List<MeterReading> findUnbilledReadingsByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(mr) FROM MeterReading mr WHERE mr.readingDate BETWEEN :startDate AND :endDate")
    long countByReadingDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.readingDate BETWEEN :startDate AND :endDate ORDER BY mr.readingDate DESC")
    List<MeterReading> findByReadingDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
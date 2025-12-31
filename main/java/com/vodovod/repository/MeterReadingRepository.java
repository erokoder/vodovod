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
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.user = :user AND COALESCE(mr.cancelled, false) = false ORDER BY mr.readingDate DESC")
    List<MeterReading> findByUserOrderByReadingDateDescending(User user);
    
    Optional<MeterReading> findTopByUserOrderByReadingDateDesc(User user);
    Optional<MeterReading> findTopByUserAndCancelledFalseOrderByReadingDateDesc(User user);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.user = :user AND COALESCE(mr.cancelled, false) = false ORDER BY mr.readingDate DESC")
    Optional<MeterReading> findTopActiveByUserOrderByReadingDateDesc(User user);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.user = :user AND mr.readingDate = :readingDate AND COALESCE(mr.cancelled, false) = false")
    Optional<MeterReading> findByUserAndReadingDate(User user, LocalDate readingDate);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.billGenerated = false AND COALESCE(mr.cancelled, false) = false")
    List<MeterReading> findReadingsWithoutBill();
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.user = :user AND mr.billGenerated = false AND COALESCE(mr.cancelled, false) = false ORDER BY mr.readingDate ASC")
    List<MeterReading> findUnbilledReadingsByUser(User user);
    
    // Ascending order per user (used for recalculation after cancellation)
    List<MeterReading> findByUserOrderByReadingDateAsc(User user);
    
    @Query("SELECT COUNT(mr) FROM MeterReading mr WHERE mr.readingDate BETWEEN :startDate AND :endDate")
    long countByReadingDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(mr) FROM MeterReading mr WHERE mr.user.organization.id = :orgId AND mr.readingDate BETWEEN :startDate AND :endDate")
    long countByOrganizationIdAndReadingDateBetween(@Param("orgId") Long organizationId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
    
    @Query("SELECT mr FROM MeterReading mr WHERE mr.readingDate BETWEEN :startDate AND :endDate ORDER BY mr.readingDate DESC")
    List<MeterReading> findByReadingDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT mr FROM MeterReading mr WHERE mr.user.organization.id = :orgId AND mr.readingDate BETWEEN :startDate AND :endDate ORDER BY mr.readingDate DESC")
    List<MeterReading> findByOrganizationIdAndReadingDateBetween(@Param("orgId") Long organizationId,
                                                                @Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate);

    @Query("SELECT mr FROM MeterReading mr JOIN mr.user u ORDER BY u.lastName ASC, u.firstName ASC, mr.readingDate DESC")
    List<MeterReading> findAllOrderByUserNameAndDateDesc();

    @Query("SELECT mr FROM MeterReading mr JOIN mr.user u WHERE u.organization.id = :orgId ORDER BY u.lastName ASC, u.firstName ASC, mr.readingDate DESC")
    List<MeterReading> findAllByOrganizationIdOrderByUserNameAndDateDesc(@Param("orgId") Long organizationId);

    // Date range filtering per user
    List<MeterReading> findByUserAndReadingDateBetweenOrderByReadingDateDesc(User user, LocalDate startDate, LocalDate endDate);
    List<MeterReading> findByUserAndReadingDateGreaterThanEqualOrderByReadingDateDesc(User user, LocalDate startDate);
    List<MeterReading> findByUserAndReadingDateLessThanEqualOrderByReadingDateDesc(User user, LocalDate endDate);

    // Date range filtering across all users
    List<MeterReading> findByReadingDateGreaterThanEqualOrderByReadingDateDesc(LocalDate startDate);
    List<MeterReading> findByReadingDateLessThanEqualOrderByReadingDateDesc(LocalDate endDate);

    @Query("SELECT mr FROM MeterReading mr WHERE mr.user.organization.id = :orgId AND mr.readingDate >= :startDate ORDER BY mr.readingDate DESC")
    List<MeterReading> findByOrganizationIdAndReadingDateGreaterThanEqualOrderByReadingDateDesc(@Param("orgId") Long organizationId,
                                                                                                @Param("startDate") LocalDate startDate);

    @Query("SELECT mr FROM MeterReading mr WHERE mr.user.organization.id = :orgId AND mr.readingDate <= :endDate ORDER BY mr.readingDate DESC")
    List<MeterReading> findByOrganizationIdAndReadingDateLessThanEqualOrderByReadingDateDesc(@Param("orgId") Long organizationId,
                                                                                             @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(mr) FROM MeterReading mr WHERE mr.user.organization.id = :orgId")
    long countByOrganizationId(@Param("orgId") Long organizationId);
}
package hr.vodovod.app.repository;

import hr.vodovod.app.entity.WaterMeter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WaterMeterRepository extends JpaRepository<WaterMeter, Long> {
    
    Optional<WaterMeter> findByMeterNumber(String meterNumber);
    
    boolean existsByMeterNumber(String meterNumber);
    
    Optional<WaterMeter> findByUserId(Long userId);
}
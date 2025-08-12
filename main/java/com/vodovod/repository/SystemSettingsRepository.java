package com.vodovod.repository;

import com.vodovod.model.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {
    
    Optional<SystemSettings> findTopByOrderByIdAsc();
}
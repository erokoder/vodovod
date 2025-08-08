package com.vodovod.repository;

import com.vodovod.model.ConfigSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigSettingRepository extends JpaRepository<ConfigSetting, Long> {
}
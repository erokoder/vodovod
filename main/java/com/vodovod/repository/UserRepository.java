package com.vodovod.repository;

import com.vodovod.model.Role;
import com.vodovod.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(Role role);
    
    List<User> findByRoleAndEnabledTrue(Role role);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.enabled = true")
    long countByRoleAndEnabled(Role role);
    
    @Query("SELECT u FROM User u WHERE u.role = 'USER' AND u.meterNumber IS NOT NULL AND u.enabled = true")
    List<User> findActiveWaterUsers();
    
    @Query("SELECT u FROM User u WHERE u.meterNumber = :meterNumber")
    Optional<User> findByMeterNumber(String meterNumber);
}
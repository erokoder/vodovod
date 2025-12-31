package com.vodovod.repository;

import com.vodovod.model.Role;
import com.vodovod.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameIgnoreCase(String username);
    
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    
    List<User> findByRole(Role role);
    
    List<User> findByRoleAndEnabledTrue(Role role);
    
    boolean existsByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);
    
    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.enabled = true")
    long countByRoleAndEnabled(Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.organization.id = :orgId AND u.role = :role AND u.enabled = true")
    long countByOrganizationAndRoleAndEnabled(@Param("orgId") Long organizationId, @Param("role") Role role);
    
    @Query("SELECT u FROM User u WHERE u.organization.id = :orgId AND u.role = 'USER' AND u.meterNumber IS NOT NULL AND u.enabled = true")
    List<User> findActiveWaterUsers(@Param("orgId") Long organizationId);

    List<User> findByOrganizationId(Long organizationId);

    List<User> findByOrganizationIdAndRoleAndEnabledTrue(Long organizationId, Role role);

    @Query("SELECT u FROM User u WHERE u.organization.id = :orgId AND u.meterNumber = :meterNumber")
    Optional<User> findByMeterNumber(@Param("orgId") Long organizationId, @Param("meterNumber") String meterNumber);

    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.role = 'ADMIN' AND u.organization.id IN :orgIds ORDER BY u.organization.id ASC, u.id ASC")
    List<User> findAdminsForOrganizations(@Param("orgIds") List<Long> organizationIds);
}
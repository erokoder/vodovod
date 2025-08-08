package hr.vodovod.app.repository;

import hr.vodovod.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(User.UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.role = 'USER' ORDER BY u.lastName, u.firstName")
    List<User> findAllCustomers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'USER'")
    long countCustomers();
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.waterMeter WHERE u.id = :id")
    Optional<User> findByIdWithWaterMeter(Long id);
}
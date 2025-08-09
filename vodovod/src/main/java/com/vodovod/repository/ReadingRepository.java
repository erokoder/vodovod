package com.vodovod.repository;

import com.vodovod.model.Reading;
import com.vodovod.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingRepository extends JpaRepository<Reading, Long> {
  List<Reading> findByUserOrderByReadingDateAsc(User user);
  Optional<Reading> findTopByUserOrderByReadingDateDesc(User user);
  List<Reading> findByUserAndReadingDateBetweenOrderByReadingDateAsc(User user, LocalDate start, LocalDate end);
}
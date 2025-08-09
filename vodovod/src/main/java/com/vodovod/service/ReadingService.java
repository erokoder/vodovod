package com.vodovod.service;

import com.vodovod.model.Reading;
import com.vodovod.model.User;
import com.vodovod.repository.ReadingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReadingService {
  private final ReadingRepository readingRepository;

  @Transactional
  public Reading addReading(User user, LocalDate date, long value) {
    readingRepository.findTopByUserOrderByReadingDateDesc(user).ifPresent(last -> {
      if (value < last.getValue()) {
        throw new IllegalArgumentException("Novo očitanje ne može biti manje od prethodnog");
      }
    });

    Reading r = Reading.builder()
        .user(user)
        .readingDate(date)
        .value(value)
        .build();
    return readingRepository.save(r);
  }
}
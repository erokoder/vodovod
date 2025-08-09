package com.vodovod.service;

import com.vodovod.model.*;
import com.vodovod.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {
  private final InvoiceRepository invoiceRepository;
  private final ReadingRepository readingRepository;
  private final ConfigSettingRepository configSettingRepository;

  @Transactional
  public Invoice generateInvoiceForUser(User user, LocalDate from, LocalDate to) {
    ConfigSetting cfg = configSettingRepository.findAll().stream().findFirst()
        .orElseThrow(() -> new IllegalStateException("Config not found"));

    List<Reading> readings = readingRepository
        .findByUserAndReadingDateBetweenOrderByReadingDateAsc(user, from, to);

    if (readings.isEmpty()) {
      throw new IllegalArgumentException("Nema očitanja u zadanom periodu");
    }

    readings.sort(Comparator.comparing(Reading::getReadingDate));
    Reading first = readings.getFirst();
    Reading last = readings.getLast();

    if (last.getValue() < first.getValue()) {
      throw new IllegalStateException("Novo očitanje ne može biti manje od prethodnog");
    }

    long consumed = last.getValue() - first.getValue();
    BigDecimal variable = cfg.getPricePerM3().multiply(BigDecimal.valueOf(consumed));
    BigDecimal total = variable;
    if (cfg.isFlatFeeEnabled()) {
      total = total.add(cfg.getFlatFeeAmount());
    }

    Invoice invoice = Invoice.builder()
        .user(user)
        .startReading(first)
        .endReading(last)
        .issuedDate(LocalDate.now())
        .pricePerM3(cfg.getPricePerM3())
        .flatFeeApplied(cfg.isFlatFeeEnabled())
        .flatFeeAmount(cfg.getFlatFeeAmount())
        .consumedM3(consumed)
        .totalAmount(total)
        .status(Invoice.InvoiceStatus.GENERATED)
        .build();

    return invoiceRepository.save(invoice);
  }
}
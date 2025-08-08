package com.vodovod.repository;

import com.vodovod.model.Invoice;
import com.vodovod.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
  List<Invoice> findByUserOrderByIssuedDateDesc(User user);
}
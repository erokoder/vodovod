package com.vodovod.repository;

import com.vodovod.model.Invoice;
import com.vodovod.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
  List<Payment> findByInvoiceOrderByPaymentDateAsc(Invoice invoice);
}
package com.vodovod.service;

import com.vodovod.dto.UserBalanceDTO;
import com.vodovod.model.User;
import com.vodovod.repository.BillRepository;
import com.vodovod.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BalanceService {

	@Autowired
	private BillRepository billRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	public UserBalanceDTO getBalanceForUser(User user) {
		if (user == null) {
			return null;
		}

		BigDecimal invoicesTotal = billRepository.sumTotalAmountByUser(user);
		BigDecimal invoicesPaid = billRepository.sumPaidAmountByUser(user);
		BigDecimal credit = paymentRepository.sumPrepaymentByUser(user);

		invoicesTotal = invoicesTotal != null ? invoicesTotal : BigDecimal.ZERO;
		invoicesPaid = invoicesPaid != null ? invoicesPaid : BigDecimal.ZERO;
		credit = credit != null ? credit : BigDecimal.ZERO;

		BigDecimal netBalance = invoicesTotal.subtract(invoicesPaid).subtract(credit);

		String status;
		BigDecimal displayAmount;
		int cmp = netBalance.compareTo(BigDecimal.ZERO);
		if (cmp > 0) {
			status = "Dužan";
			displayAmount = netBalance;
		} else if (cmp < 0) {
			status = "Pretplaćen";
			displayAmount = netBalance.abs();
		} else {
			status = "Plaćeno sve";
			displayAmount = BigDecimal.ZERO;
		}

		return new UserBalanceDTO(
				user.getId(),
				user.getFullName(),
				user.getMeterNumber(),
				displayAmount,
				status
		);
	}
}


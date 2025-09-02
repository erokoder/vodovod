package com.vodovod.service;

import com.vodovod.dto.BillPreviewDTO;
import com.vodovod.model.Bill;
import com.vodovod.model.BillStatus;
import com.vodovod.model.MeterReading;
import com.vodovod.model.SystemSettings;
import com.vodovod.model.User;
import com.vodovod.repository.BillRepository;
import com.vodovod.repository.MeterReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private MeterReadingRepository meterReadingRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private PaymentService paymentService;

    public List<BillPreviewDTO> previewAllBills(Optional<Long> optionalUserId) {
        List<User> users;
        if (optionalUserId.isPresent()) {
            users = userService.getUserById(optionalUserId.get()).map(List::of).orElseGet(List::of);
        } else {
            users = userService.getActiveWaterUsers();
        }
        SystemSettings settings = settingsService.getCurrentSettingsOrNew();
        List<BillPreviewDTO> previews = new ArrayList<>();
        for (User user : users) {
            if (user.getMeterNumber() == null) {
                continue;
            }
            List<MeterReading> unbilled = meterReadingRepository.findUnbilledNonCancelledReadingsByUser(user);
            if (unbilled.isEmpty()) {
                continue;
            }
            unbilled.sort(Comparator.comparing(MeterReading::getReadingDate));
            MeterReading first = unbilled.get(0);
            MeterReading last = unbilled.get(unbilled.size() - 1);

            BigDecimal previous = first.getPreviousReadingValue() == null ? BigDecimal.ZERO : first.getPreviousReadingValue();
            BigDecimal current = last.getReadingValue();
            BigDecimal consumption = current.subtract(previous);
            if (consumption.signum() < 0) {
                continue;
            }

            BillPreviewDTO dto = new BillPreviewDTO();
            dto.setUserId(user.getId());
            dto.setUserName(user.getFullName());
            dto.setPeriodFrom(first.getReadingDate());
            dto.setPeriodTo(last.getReadingDate());
            dto.setPreviousReading(previous);
            dto.setCurrentReading(current);
            dto.setConsumption(consumption);
            BigDecimal price = settings.getWaterPricePerM3() != null ? settings.getWaterPricePerM3() : BigDecimal.ZERO;
            dto.setWaterPricePerM3(price);
            BigDecimal waterAmount = price.multiply(consumption);
            dto.setWaterAmount(waterAmount);
            dto.setFixedFeeApplied(settings.isUseFixedFee());
            dto.setFixedFeeAmount(settings.isUseFixedFee() ? settings.getFixedFee() : BigDecimal.ZERO);
            dto.setTotalAmount(waterAmount.add(dto.getFixedFeeAmount()));

            LocalDate issueDate = LocalDate.now();
            LocalDate dueDate = settings.getBillDueDays() != null ? issueDate.plusDays(settings.getBillDueDays()) : issueDate.plusDays(30);
            dto.setIssueDate(issueDate);
            dto.setDueDate(dueDate);
            dto.setEndReadingId(last.getId());

            previews.add(dto);
        }
        return previews;
    }

    @Transactional
    public List<Bill> generateBillsFromPreview(List<BillPreviewDTO> previews, String createdBy) {
        SystemSettings settings = settingsService.getCurrentSettingsOrNew();
        List<Bill> saved = new ArrayList<>();
        Map<Integer, Integer> yearToLastSequence = new HashMap<>();
        for (BillPreviewDTO dto : previews) {
            Optional<User> userOpt = userService.getUserById(dto.getUserId());
            if (userOpt.isEmpty()) {
                continue;
            }
            User user = userOpt.get();
            Bill bill = new Bill();
            bill.setUser(user);
            bill.setIssueDate(dto.getIssueDate());
            bill.setDueDate(dto.getDueDate());
            bill.setPeriodFrom(dto.getPeriodFrom());
            bill.setPeriodTo(dto.getPeriodTo());
            bill.setPreviousReading(dto.getPreviousReading());
            bill.setCurrentReading(dto.getCurrentReading());
            bill.setConsumption(dto.getConsumption());
            bill.setWaterPricePerM3(dto.getWaterPricePerM3());
            bill.setWaterAmount(dto.getWaterAmount());
            bill.setFixedFee(dto.getFixedFeeAmount());
            bill.setTotalAmount(dto.getTotalAmount());
            bill.setStatus(BillStatus.PENDING);
            bill.setCreatedBy(createdBy);
            bill.setAccountNumber(settings.getAccountNumber());
            bill.setPaidAmount(java.math.BigDecimal.ZERO);

            // Generate bill number in the form x/YYYY where x increments per year
            int year = bill.getIssueDate() != null ? bill.getIssueDate().getYear() : LocalDate.now().getYear();
            Integer lastSeq = yearToLastSequence.get(year);
            if (lastSeq == null) {
                lastSeq = billRepository.findMaxSequenceForYear(year);
            }
            int nextSeq = lastSeq + 1;
            yearToLastSequence.put(year, nextSeq);
            bill.setBillNumber(nextSeq + "/" + year);

            billRepository.save(bill);

            // Mark all readings up to endReadingId as billed for this user
            List<MeterReading> unbilled = meterReadingRepository.findUnbilledNonCancelledReadingsByUser(user);
            for (MeterReading r : unbilled) {
                if (!r.isBillGenerated()) {
                    r.setBillGenerated(true);
                    meterReadingRepository.save(r);
                }
                if (r.getId().equals(dto.getEndReadingId())) {
                    break;
                }
            }

            // Apply any existing prepayments to this bill
            paymentService.applyPrepaymentsToBill(bill, createdBy);

            saved.add(bill);
        }
        return saved;
    }
}
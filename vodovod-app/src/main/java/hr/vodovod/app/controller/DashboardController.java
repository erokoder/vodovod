package hr.vodovod.app.controller;

import hr.vodovod.app.repository.InvoiceRepository;
import hr.vodovod.app.repository.ReadingRepository;
import hr.vodovod.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReadingRepository readingRepository;
    
    @GetMapping("/")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Statistike
        long totalCustomers = userRepository.countCustomers();
        long unpaidInvoices = invoiceRepository.countUnpaidInvoices();
        long paidInvoices = invoiceRepository.countPaidInvoices();
        long uninvoicedReadings = readingRepository.countUninvoicedReadings();
        
        BigDecimal unpaidAmount = invoiceRepository.sumUnpaidAmount();
        BigDecimal paidAmount = invoiceRepository.sumPaidAmount();
        
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("unpaidInvoices", unpaidInvoices);
        model.addAttribute("paidInvoices", paidInvoices);
        model.addAttribute("uninvoicedReadings", uninvoicedReadings);
        model.addAttribute("unpaidAmount", unpaidAmount != null ? unpaidAmount : BigDecimal.ZERO);
        model.addAttribute("paidAmount", paidAmount != null ? paidAmount : BigDecimal.ZERO);
        
        // Zadnji neplaćeni računi
        model.addAttribute("recentUnpaidInvoices", 
            invoiceRepository.findByStatus(hr.vodovod.app.entity.Invoice.InvoiceStatus.UNPAID)
                .stream()
                .limit(5)
                .toList()
        );
        
        return "dashboard/index";
    }
}
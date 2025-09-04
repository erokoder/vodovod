package com.vodovod.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vodovod.model.Bill;
import com.vodovod.model.SystemSettings;
import com.vodovod.model.User;
import com.vodovod.repository.BillRepository;
import com.vodovod.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class BillPdfService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public byte[] createBillPdf(Bill bill, SystemSettings settings) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            InputStream templateStream = BillPdfService.class.getResourceAsStream("/tamplate_invoice-2.pdf");
            if (templateStream == null) {
                throw new RuntimeException("PDF template '/tamplate_invoice-2.pdf' nije pronađen u resources.");
            }

            PdfReader pdfReader = new PdfReader(templateStream);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, baos);
            PdfContentByte canvas = pdfStamper.getOverContent(1);

            // Fonts
            BaseFont base = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1250, BaseFont.EMBEDDED);
            Font font = new Font(base, 10, Font.NORMAL, BaseColor.BLACK);
            Font font2 = new Font(base, 13, Font.NORMAL, BaseColor.BLACK);
            Font font3 = new Font(base, 11, Font.NORMAL, BaseColor.BLACK);
            Font font4 = new Font(base, 20, Font.BOLD, BaseColor.BLACK);

            // Formatters
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("bs", "BA"));

            // Extract values
            User user = bill.getUser();
            String client = safe(user.getFullName());
            String addressLine1 = "";
            String addressLine2 = "";
            if (user.getAddress() != null && !user.getAddress().isBlank()) {
                String[] parts = user.getAddress().split(",|\n");
                addressLine1 = parts.length > 0 ? parts[0].trim() : "";
                addressLine2 = parts.length > 1 ? parts[1].trim() : "";
            }
            if (addressLine1.isBlank()) addressLine1 = "Humac b.b.";
            if (addressLine2.isBlank()) addressLine2 = "88320 Ljubuški";

            String dateOd = bill.getPeriodFrom() != null ? bill.getPeriodFrom().format(df) : "";
            String dateDo = bill.getPeriodTo() != null ? bill.getPeriodTo().format(df) : "";
            String serialNumber = safe(user.getMeterNumber());

            String oldValue = toStr(bill.getPreviousReading());
            String newValue = toStr(bill.getCurrentReading());
            String potrosnja = toStr(bill.getConsumption());

            BigDecimal waterAmount = nz(bill.getWaterAmount());
            BigDecimal fixedFee = nz(bill.getFixedFee());
            BigDecimal total = waterAmount.add(fixedFee);
            BigDecimal waterPrice = nz(bill.getWaterPricePerM3());

            // Compute user's net credit/debt (dug): positive -> preplata, negative -> dug
            BigDecimal totalAll = billRepository.sumTotalAmountByUser(user);
            if (totalAll == null) totalAll = BigDecimal.ZERO;
            BigDecimal paidAll = billRepository.sumPaidAmountByUser(user);
            if (paidAll == null) paidAll = BigDecimal.ZERO;
            BigDecimal outstandingDebt = totalAll.subtract(paidAll); // >0 means debt
            BigDecimal prepayment = paymentRepository.sumPrepaymentByUser(user);
            if (prepayment == null) prepayment = BigDecimal.ZERO;
            BigDecimal dugDecimal = prepayment.subtract(outstandingDebt); // positive credit, negative debt

            double iznos = waterAmount.doubleValue();
            double pausal = fixedFee.doubleValue();
            double cijena_vode = waterPrice.doubleValue();
            double dug = dugDecimal.doubleValue();

            // Header and meta mapped to template positions (legacy layout)
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(settings.getCompanyName(), font4), 60, 760, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(client, font2), 60, 670, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(addressLine1, font2), 60, 655, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(addressLine2, font2), 60, 640, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(settings.getCompanyPhone(), font), 385, 630, 0);

            if (!dateOd.isBlank() && !oldValue.isBlank()) {
                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(dateOd + " -> " + oldValue, font), 60, 550, 0);
            }
            if (!dateDo.isBlank() && !newValue.isBlank()) {
                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(dateDo + " -> " + newValue, font), 60, 520, 0);
            }

            if (bill.getIssueDate() != null) {
                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("Datum kreiranja računa: " + bill.getIssueDate().format(df), font), 60, 600, 0);
            }
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("Brojilo: " + serialNumber, font), 60, 585, 0);

            // Table-like numbers on template
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(potrosnja, font), 385, 535, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(currency.format(iznos), font), 480, 535, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(currency.format(pausal), font), 480, 515, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(currency.format(iznos + pausal), font), 480, 452, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(currency.format(cijena_vode), font), 435, 535, 0);

            // Debt/credit info block
            if (Math.abs(dug) < 0.005) {
                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("Nema prethodni dugovanja", font), 60, 402, 0);
            } else if (dug < 0) {
                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("Vaš dug na datum izdavanja ovog računa je: " + currency.format(Math.abs(dug)) + " , molimo Vas da izmirite svoje obaveze.", font), 60, 402, 0);
            } else {
                double platit = (iznos + pausal) - dug;
                if (platit <= 0) {
                    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("U pretplati ste imali: " + currency.format(Math.abs(dug)) + " nakon ovog računa ostane vam još: " + currency.format(Math.abs((iznos + pausal) - dug)), font), 60, 402, 0);
                } else {
                    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase("U pretplati ste imali: " + currency.format(Math.abs(dug)) + " nakon ovog računa ostane vam još: " + currency.format(0), font), 60, 402, 0);
                }
            }

            // Additional notes
            String dodatak = bill.getNotes() == null ? "" : bill.getNotes();
            ColumnText ct = new ColumnText(canvas);
            ct.setSimpleColumn(new Phrase(dodatak, font), 533f, 100f, 60f, 377f, 10, Element.ALIGN_JUSTIFIED);
            ct.go();

            // Payment slip (uplatnica) area mapping
            String payerLine = client + (addressLine1.isBlank() ? "" : ", " + addressLine1) + (addressLine2.isBlank() ? "" : " " + addressLine2);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(payerLine, font3), 31, 237, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(serialNumber, font3), 31, 183, 0);
            // Broj računa u poljima (donji dio) — uzmi iz admin panela (billNumber)
            String billNumberStr = safe(bill.getAccountNumber());
            if (!billNumberStr.isBlank()) {
                String compact = billNumberStr.replaceAll("\\s+", "");
                drawCharactersInBoxes(canvas, compact, 387f, 196f, 12f, font3);
            }

            String companyName = safe(settings.getCompanyName());
            String companyAddress = settings.getCompanyAddress();
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(companyName, font3), 31, 127, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(companyAddress, font3), 31, 115, 0);

            double platit = (iznos + pausal) - dug;
            if (dug > 0) {
                if (platit >= 0) {
                    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(currency.format(-platit), font3), 320, 168, 0);
                } else {
                    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(currency.format(0), font3), 320, 168, 0);
                }
            } else {
                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(currency.format(iznos + pausal), font3), 320, 168, 0);
            }

            pdfStamper.close();
            pdfReader.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Greška pri generiranju PDF-a", e);
        }
    }

    private static String toStr(BigDecimal value) {
        if (value == null) return "";
        BigDecimal stripped = value.stripTrailingZeros();
        return stripped.scale() <= 0 ? stripped.toPlainString() : stripped.toPlainString();
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private static String safe(String v) { return v == null ? "" : v; }

    private static void drawCharactersInBoxes(PdfContentByte canvas, String text, float startX, float baselineY, float charSpacing, Font font) {
        if (text == null || text.isEmpty()) {
            return;
        }
        float x = startX;
        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(ch, font), x, baselineY, 0);
            x += charSpacing;
        }
    }
}
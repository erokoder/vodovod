package com.vodovod.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vodovod.model.Bill;
import com.vodovod.model.SystemSettings;
import com.vodovod.model.User;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class BillPdfService {

    public byte[] createBillPdf(Bill bill, SystemSettings settings) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

            // Header
            Paragraph company = new Paragraph(safe(settings.getCompanyName(), "Vodovod"), sectionTitleFont);
            document.add(company);
            if (settings.getCompanyAddress() != null) {
                document.add(new Paragraph(settings.getCompanyAddress(), smallFont));
            }
            if (settings.getCompanyPhone() != null || settings.getCompanyEmail() != null) {
                String contact = (settings.getCompanyPhone() != null ? settings.getCompanyPhone() : "");
                if (settings.getCompanyEmail() != null) {
                    contact = (contact.isEmpty() ? "" : contact + " | ") + settings.getCompanyEmail();
                }
                if (!contact.isEmpty()) document.add(new Paragraph(contact, smallFont));
            }
            document.add(Chunk.NEWLINE);

            Paragraph title = new Paragraph("Račun za vodu", titleFont);
            document.add(title);

            // Bill meta and customer section
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            User u = bill.getUser();
            PdfPTable meta = new PdfPTable(new float[]{1f, 1f});
            meta.setWidthPercentage(100);
            PdfPCell left = new PdfPCell();
            left.setBorder(Rectangle.NO_BORDER);
            left.addElement(new Paragraph(safe(u.getFullName(), ""), normalFont));
            if (u.getAddress() != null) left.addElement(new Paragraph(u.getAddress(), normalFont));
            if (u.getPhoneNumber() != null) left.addElement(new Paragraph(u.getPhoneNumber(), normalFont));

            PdfPCell right = new PdfPCell();
            right.setBorder(Rectangle.NO_BORDER);
            right.addElement(new Paragraph("Broj računa: " + safe(bill.getBillNumber(), "-"), normalFont));
            right.addElement(new Paragraph("Datum izdavanja: " + (bill.getIssueDate() != null ? bill.getIssueDate().format(df) : ""), normalFont));
            right.addElement(new Paragraph("Rok plaćanja: " + (bill.getDueDate() != null ? bill.getDueDate().format(df) : ""), normalFont));
            right.addElement(new Paragraph("Broj vodomjera: " + safe(u.getMeterNumber(), ""), normalFont));

            meta.addCell(left);
            meta.addCell(right);
            document.add(meta);
            document.add(Chunk.NEWLINE);

            // Period and readings
            Paragraph period = new Paragraph(
                String.format("Period: %s - %s    |    Očitanje: %s -> %s",
                    bill.getPeriodFrom() != null ? bill.getPeriodFrom().format(df) : "",
                    bill.getPeriodTo() != null ? bill.getPeriodTo().format(df) : "",
                    toStr(bill.getPreviousReading()),
                    toStr(bill.getCurrentReading())
                ), normalFont);
            document.add(period);
            document.add(Chunk.NEWLINE);

            // Items table
            PdfPTable table = new PdfPTable(new float[]{3f, 1f, 1f, 1f, 1f});
            table.setWidthPercentage(100);
            addHeaderCell(table, "Opis");
            addHeaderCell(table, "Jedinica");
            addHeaderCell(table, "Količina");
            addHeaderCell(table, "Cijena");
            addHeaderCell(table, "Ukupno");

            addBodyCell(table, "Potrošnja Vode");
            addBodyCell(table, "m3");
            addBodyCell(table, toStr(bill.getConsumption()));
            addBodyCell(table, money(bill.getWaterPricePerM3()));
            addBodyCell(table, money(bill.getWaterAmount()));

            if (bill.getFixedFee() != null && bill.getFixedFee().compareTo(BigDecimal.ZERO) > 0) {
                addBodyCell(table, "Paušalno (jednom godišnje)");
                addBodyCell(table, "");
                addBodyCell(table, "");
                addBodyCell(table, "");
                addBodyCell(table, money(bill.getFixedFee()));
            }

            // Total row
            PdfPCell totalLabel = new PdfPCell(new Phrase("Ukupno", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
            totalLabel.setColspan(4);
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalLabel);
            PdfPCell totalValue = new PdfPCell(new Phrase(money(bill.getTotalAmount()), new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
            totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalValue);
            document.add(table);

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Pregled stanja duga potrošača: " + (bill.isOverdue() ? "Dospjelo" : "Nema prethodnih dugovanja"), smallFont));
            document.add(Chunk.NEWLINE);

            // Payment slip (Uplatnica)
            document.add(new Paragraph("Uplatnica", sectionTitleFont));
            document.add(Chunk.NEWLINE);

            PdfPTable slip = new PdfPTable(new float[]{2f, 2f});
            slip.setWidthPercentage(100);

            // Left column: Payer and purpose
            PdfPCell payer = new PdfPCell();
            payer.addElement(new Paragraph("Uplatitelj", smallFont));
            String payerName = safe(u.getFullName(), "");
            String payerAddress = safe(u.getAddress(), "");
            payer.addElement(new Paragraph(payerName + (payerAddress.isEmpty()?"":"; " + payerAddress), normalFont));
            payer.addElement(Chunk.NEWLINE);
            payer.addElement(new Paragraph("Svrha doznake", smallFont));
            String svrha = "Voda - broj vodomjera: " + safe(u.getMeterNumber(), "");
            payer.addElement(new Paragraph(svrha, normalFont));
            payer.setPadding(8);
            slip.addCell(payer);

            // Right column: Recipient and account
            PdfPCell recipient = new PdfPCell();
            recipient.addElement(new Paragraph("Račun primatelja", smallFont));
            recipient.addElement(new Paragraph(safe(settings.getAccountNumber(), ""), normalFont));
            recipient.addElement(Chunk.NEWLINE);
            recipient.addElement(new Paragraph("Primatelj", smallFont));
            String primatelj = safe(settings.getCompanyName(), "") +
                    (settings.getCompanyAddress() != null ? ", " + settings.getCompanyAddress() : "");
            recipient.addElement(new Paragraph(primatelj, normalFont));
            recipient.addElement(Chunk.NEWLINE);
            recipient.addElement(new Paragraph("Iznos", smallFont));
            recipient.addElement(new Paragraph(money(bill.getTotalAmount()) + " KM", normalFont));
            recipient.setPadding(8);
            slip.addCell(recipient);

            document.add(slip);

            document.close();
            writer.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Greška pri generiranju PDF-a", e);
        }
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new BaseColor(230, 240, 250));
        table.addCell(cell);
    }

    private static void addBodyCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.HELVETICA, 10)));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
    }

    private static String money(BigDecimal value) {
        if (value == null) return "-";
        return String.format("%,.2f", value).replace(',', 'X').replace('.', ',').replace('X', '.');
    }

    private static String toStr(BigDecimal value) {
        return value == null ? "-" : value.stripTrailingZeros().toPlainString();
    }

    private static String safe(String v, String def) { return v == null || v.isBlank() ? def : v; }
}
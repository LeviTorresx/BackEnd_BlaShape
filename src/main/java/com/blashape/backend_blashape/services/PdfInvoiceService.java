package com.blashape.backend_blashape.services;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.blashape.backend_blashape.entitys.Payment;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Service
public class PdfInvoiceService {

    public byte[] generateInvoice(Payment payment) {
        try {
            String customerName = payment.getCarpenter().getName();
            String email = payment.getCarpenter().getEmail();
            String invoiceId = "INV-" + payment.getPaymentId();
            double amount = payment.getAmount();

            String html = buildHtml(customerName, email, invoiceId, amount, payment);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando factura PDF", e);
        }
    }

    private String buildHtml(String customerName, String email, String invoiceId, double amount, Payment payment) {

        String logoUrl = "https://res.cloudinary.com/dr63i7owa/image/upload/v1773726191/logo_BS_uxob0a.png";

        String productName = (payment.getProduct() != null)
                ? payment.getProduct().getProductName()
                : payment.getPlan().getPlanName();

        String currency = payment.getCurrency();

        String date = payment.getCreatedAt()
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; margin:0; padding:0; background-color:#f5f5f5;">
                
                <div style="max-width:800px; margin:30px auto; background:white; padding:30px; border-radius:10px;">

                    <!-- HEADER -->
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <img src="%s" style="height:60px;" />
                        <div style="text-align:right;">
                            <h2 style="margin:0; color:#9117e4;">FACTURA</h2>
                            <p style="margin:0;">%s</p>
                        </div>
                    </div>

                    <hr style="margin:20px 0;"/>

                    <!-- CLIENTE -->
                    <div style="margin-bottom:20px;">
                        <p><strong>Cliente:</strong> %s</p>
                        <p><strong>Email:</strong> %s</p>
                        <p><strong>Factura ID:</strong> %s</p>
                    </div>

                    <!-- TABLA -->
                    <table style="width:100%%; border-collapse: collapse; margin-top:20px;">
                        <thead>
                            <tr style="background-color:#9117e4; color:white;">
                                <th style="padding:12px; text-align:left;">Descripción</th>
                                <th style="padding:12px; text-align:right;">Precio</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr style="border-bottom:1px solid #ddd;">
                                <td style="padding:12px;">%s</td>
                                <td style="padding:12px; text-align:right;">%s %.2f</td>
                            </tr>
                        </tbody>
                    </table>

                    <!-- TOTAL -->
                    <div style="margin-top:30px; text-align:right;">
                        <h2 style="color:#9117e4;">Total: %s %.2f</h2>
                    </div>

                    <hr style="margin:30px 0;"/>

                    <!-- FOOTER -->
                    <div style="text-align:center; font-size:12px; color:#777;">
                        <p>Gracias por usar Blashape</p>
                        <p>Esta es una factura generada automáticamente.</p>
                    </div>

                </div>

            </body>
            </html>
            """,
                logoUrl,
                date,
                customerName,
                email,
                invoiceId,
                productName,
                currency, amount,
                currency, amount
        );
    }
}
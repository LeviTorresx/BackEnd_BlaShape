package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.entitys.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final PdfInvoiceService pdfService;
    private final EmailService emailService;

    /*public void sendInvoice(Payment payment) {

        try {
            byte[] pdf = pdfService.generateInvoice(payment);

            String html = buildEmail(payment);

            emailService.sendEmailWithAttachment(
                    payment.getCarpenter().getEmail(),
                    "Factura de compra - Blashape",
                    html,
                    pdf
            );

        } catch (Exception e) {
            throw new RuntimeException("Error generando factura", e);
        }
    }

    private String buildEmail(Payment payment) {
        return """
        <div style="font-family: Arial; text-align:center;">
            <h2 style="color:#4c1d95;">Factura generada</h2>
            <p>Hola %s,</p>
            <p>Tu pago ha sido procesado correctamente.</p>
            <p>Adjunto encontrarás tu factura en PDF.</p>
            <br/>
            <p style="font-size:12px;color:gray;">
                Gracias por confiar en Blashape
            </p>
        </div>
        """.formatted(payment.getCarpenter().getName());
    }*/
}
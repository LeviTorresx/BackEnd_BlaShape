package com.blashape.backend_blashape.services;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.blashape.backend_blashape.DTOs.ReceiptDTO;
import org.springframework.stereotype.Service;

import com.blashape.backend_blashape.DTOs.InvoiceDTO;
import com.blashape.backend_blashape.entitys.Payment;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Service
public class PdfInvoiceService {

    private static final String COMPANY_NAME    = "Maderas Blashape";
    private static final String COMPANY_NIT     = "En tramite";   // actualizas cuando lo tengan
    private static final String COMPANY_ADDRESS = "Cra 19 #07-44 Caucasia Antioquia"; // la pones tú
    private static final String LOGO_URL =
            "https://res.cloudinary.com/dr63i7owa/image/upload/v1773726191/logo_BS_uxob0a.png";

    // Fallbacks si el Workshop no tiene datos completos
    private static final String DEFAULT_COMPANY_NAME    = "Blashape";
    private static final String DEFAULT_COMPANY_NIT     = "NIT: 900.XXX.XXX-X";   // ← reemplaza
    private static final String DEFAULT_COMPANY_ADDRESS = "Bogotá D.C., Colombia"; // ← reemplaza

    // ─────────────────────────────────────────────────────────────────────────
    // FACTURA FORMAL
    // ─────────────────────────────────────────────────────────────────────────
    public byte[] generateFormalInvoice(InvoiceDTO data) {
        try {
            return renderToPdf(buildFormalInvoiceHtml(data));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generando factura PDF: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RECIBO DE PAGO (existente, HTML mejorado)
    // ─────────────────────────────────────────────────────────────────────────
    public byte[] generateInvoice(Payment payment) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    .withZone(ZoneId.systemDefault());

            String productName = payment.getProduct() != null
                    ? payment.getProduct().getProductName()
                    : "Plan " + payment.getPlan().getPlanName();

            String paymentTypeLabel = payment.getProduct() != null
                    ? "Compra unica" : "Suscripcion";

            String subscriptionHtml = "";
            if (payment.getSubscription() != null) {
                subscriptionHtml = String.format(
                        "<p style='margin:3px 0;'><strong>Periodo:</strong> %s - %s</p>",
                        fmt.format(payment.getSubscription().getStartDate()),
                        fmt.format(payment.getSubscription().getEndDate())
                );
            }

            return renderToPdf(buildReceiptHtml(
                    payment.getCarpenter().getName(),
                    payment.getCarpenter().getEmail(),
                    "INV-" + payment.getPaymentId(),
                    payment.getAmount(),
                    productName,
                    paymentTypeLabel,
                    subscriptionHtml,
                    fmt.format(payment.getCreatedAt())
            ));
        } catch (Exception e) {
            throw new RuntimeException("Error generando recibo PDF", e);
        }
    }

    public byte[] generateInvoice(ReceiptDTO dto) {
        try {
            String subscriptionHtml = "";
            if ("SUBSCRIPTION".equals(dto.getPaymentType())
                    && dto.getPeriodStart() != null
                    && dto.getPeriodEnd() != null) {
                subscriptionHtml = String.format(
                        "<p style='margin:3px 0;'><strong>Periodo:</strong> %s - %s</p>",
                        dto.getPeriodStart(), dto.getPeriodEnd()
                );
            }

            String paymentTypeLabel = "SUBSCRIPTION".equals(dto.getPaymentType())
                    ? "Suscripcion" : "Compra unica";

            return renderToPdf(buildReceiptHtml(
                    dto.getCustomerName(),
                    dto.getCustomerEmail(),
                    dto.getReceiptId(),
                    dto.getAmount(),
                    dto.getProductName(),
                    paymentTypeLabel,
                    subscriptionHtml,
                    dto.getDate()
            ));
        } catch (Exception e) {
            throw new RuntimeException("Error generando recibo PDF", e);
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // HTML — FACTURA FORMAL
    // ─────────────────────────────────────────────────────────────────────────
    private String buildFormalInvoiceHtml(InvoiceDTO d) {

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.systemDefault());

        String invoiceDate = fmt.format(d.getInvoiceDate());

        String periodHtml = "";
        if (d.getPeriodStart() != null && d.getPeriodEnd() != null) {
            periodHtml = "<br/><span style='color:#888888; font-size:11px;'>Periodo: "
                    + fmt.format(d.getPeriodStart()) + " - " + fmt.format(d.getPeriodEnd())
                    + "</span>";
        }

        double subtotal  = d.getUnitCost();
        double discount  = subtotal * (d.getDiscountPercent() / 100.0);
        double afterDisc = subtotal - discount;
        double vat       = afterDisc * (d.getVatPercent() / 100.0);
        double total     = afterDisc + vat;

        String discountRow;
        if (d.getDiscountPercent() > 0) {
            discountRow = "<tr>"
                    + "<td style='padding:8px 12px; text-align:left;'>Descuento (" + (int)d.getDiscountPercent() + "%)</td>"
                    + "<td style='padding:8px 12px; text-align:center;'>1</td>"
                    + "<td style='padding:8px 12px; text-align:right;'>-</td>"
                    + "<td style='padding:8px 12px; text-align:right; color:#c0392b;'>- " + formatCOP(discount) + "</td>"
                    + "</tr>";
        } else {
            discountRow = "<tr>"
                    + "<td style='padding:8px 12px; text-align:left;'>Descuento</td>"
                    + "<td style='padding:8px 12px; text-align:center;'>-</td>"
                    + "<td style='padding:8px 12px; text-align:right;'>-</td>"
                    + "<td style='padding:8px 12px; text-align:right; color:#888888;'>No aplica</td>"
                    + "</tr>";
        }

        String workshopName    = nvl(d.getWorkshopName(),    "-");
        String workshopAddress = nvl(d.getWorkshopAddress(), "-");
        String paymentMethod   = nvl(d.getPaymentMethod(),   "Pago en linea - Stripe");

        String css = """
        <style>
          * { margin:0; padding:0; }
          body { font-family:Arial,sans-serif; font-size:12px; color:#2d2d2d; background:#ffffff; }
          .info-box { border:1px solid #e5d5fa; }
          .info-box-header { background-color:#9117e4; color:#ffffff; font-size:10px;
                             font-weight:bold; letter-spacing:1px;
                             text-transform:uppercase; padding:6px 12px; }
          .data-table { width:100%; border-collapse:collapse; }
          .data-table thead tr { background-color:#9117e4; }
          .data-table thead th { padding:9px 12px; font-size:11px; font-weight:bold; color:#ffffff; }
          .data-table tbody tr { border-bottom:1px solid #f0e5fc; }
          .data-table tbody tr.even { background-color:#fdf7ff; }
          .totals-table { width:45%; border-collapse:collapse; border:1px solid #e5d5fa; }
          .totals-table td { padding:7px 14px; font-size:12px; border-bottom:1px solid #f0e5fc; }
          .grand td { background-color:#9117e4; color:#ffffff; font-weight:bold;
                      font-size:13px; border-bottom:none; }
          .badge { background-color:#f5eeff; border-left:4px solid #9117e4;
                   padding:8px 16px; font-size:12px; color:#555555; margin:10px 20px; }
          .payment-band { background-color:#f5eeff; padding:9px 20px;
                          font-size:12px; color:#555555; margin:10px 20px 0 20px; }
          .footer { background-color:#1a1a2e; color:#aaaaaa; text-align:center;
                    padding:14px 20px; font-size:10px; line-height:1.8; margin-top:14px; }
          .section-label { font-size:10px; font-weight:bold; text-transform:uppercase;
                           letter-spacing:1px; color:#9117e4;
                           padding:10px 20px 4px 20px; border-bottom:1.5px solid #e5d5fa; }
        </style>
        """;

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'/>" + css + "</head><body style='margin:0; padding:0;'>"

                // HEADER
                + "<table style='width:100%; background-color:#9117e4;' cellpadding='0' cellspacing='0'><tr>"
                + "<td style='padding:16px 20px; vertical-align:middle;'>"
                + "<img src='" + LOGO_URL + "' style='height:44px;'/></td>"
                + "<td style='padding:16px 20px; text-align:right; vertical-align:middle;'>"
                + "<div style='color:#ffffff; font-size:22px; font-weight:bold; letter-spacing:3px;'>FACTURA</div>"
                + "<div style='color:#ffffff; font-size:11px; margin-top:3px;'>Fecha de emision: " + invoiceDate + "</div>"
                + "</td></tr></table>"

                // BADGE
                + "<div class='badge'>Numero de factura: "
                + "<span style='color:#9117e4; font-weight:bold; font-size:13px;'>" + d.getInvoiceId() + "</span></div>"

                // INFO GRID
                + "<table style='width:100%; border-collapse:collapse; margin:0 0 10px 0;'><tr>"

                // Columna cliente
                + "<td style='width:50%; padding:0 10px 0 20px; vertical-align:top;'>"
                + "<div class='info-box'>"
                + "<div class='info-box-header'>Datos del cliente</div>"
                + "<div style='padding:10px 12px;'>"
                + "<p style='margin:3px 0; color:#444;'><strong>Nombre:</strong> " + d.getCustomerName() + "</p>"
                + "<p style='margin:3px 0; color:#444;'><strong>Identificacion:</strong> " + d.getCustomerDni() + "</p>"
                + "<p style='margin:3px 0; color:#444;'><strong>Email:</strong> " + d.getCustomerEmail() + "</p>"
                + "<p style='margin:3px 0; color:#444;'><strong>Taller:</strong> " + workshopName + "</p>"
                + "<p style='margin:3px 0; color:#444;'><strong>Direccion:</strong> " + workshopAddress + "</p>"
                + "</div></div></td>"

                // Columna emisor
                + "<td style='width:50%; padding:0 20px 0 10px; vertical-align:top;'>"
                + "<div class='info-box'>"
                + "<div class='info-box-header'>Emisor</div>"
                + "<div style='padding:10px 12px;'>"
                + "<p style='margin:3px 0; color:#444;'><strong>Empresa:</strong> " + COMPANY_NAME + "</p>"
                + "<p style='margin:3px 0; color:#444;'><strong>NIT:</strong> " + COMPANY_NIT + "</p>"
                + "<p style='margin:3px 0; color:#444;'><strong>Direccion:</strong> " + COMPANY_ADDRESS + "</p>"
                + "</div></div></td>"

                + "</tr></table>"

                // DETALLE DEL SERVICIO
                + "<div class='section-label'>Detalle del servicio</div>"
                + "<div style='margin:0 20px;'>"
                + "<table class='data-table'><thead><tr>"
                + "<th style='text-align:left; width:46%;'>Concepto</th>"
                + "<th style='text-align:center; width:10%;'>Cant.</th>"
                + "<th style='text-align:right; width:22%;'>Precio unitario</th>"
                + "<th style='text-align:right; width:22%;'>Total</th>"
                + "</tr></thead><tbody>"
                + "<tr><td style='padding:9px 12px;'>" + d.getConcept() + periodHtml + "</td>"
                + "<td style='padding:9px 12px; text-align:center;'>1</td>"
                + "<td style='padding:9px 12px; text-align:right;'>" + formatCOP(subtotal) + "</td>"
                + "<td style='padding:9px 12px; text-align:right;'>" + formatCOP(subtotal) + "</td></tr>"
                + discountRow
                + "</tbody></table></div>"

                // TOTALES
                + "<table style='width:45%; border-collapse:collapse; border:1px solid #e5d5fa;"
                + " margin-top:10px; margin-left:55%; margin-right:20px;'>"
                + "<tr><td style='padding:7px 14px;'>Subtotal</td>"
                + "<td style='padding:7px 14px; text-align:right;'>" + formatCOP(afterDisc) + "</td></tr>"
                + "<tr><td style='padding:7px 14px; border-bottom:1px solid #f0e5fc;'>IVA (" + (int)d.getVatPercent() + "%)</td>"
                + "<td style='padding:7px 14px; text-align:right; border-bottom:1px solid #f0e5fc;'>" + formatCOP(vat) + "</td></tr>"
                + "<tr><td style='padding:7px 14px; background-color:#9117e4; color:#ffffff; font-weight:bold; font-size:13px;'>TOTAL</td>"
                + "<td style='padding:7px 14px; background-color:#9117e4; color:#ffffff; font-weight:bold; font-size:13px; text-align:right;'>" + formatCOP(total) + "</td></tr>"
                + "</table>"

                // FORMA DE PAGO
                + "<div class='payment-band'>"
                + "<strong style='color:#7a0cc0;'>Forma de pago:</strong> " + paymentMethod
                + "</div>"

                // FOOTER
                + "<div class='footer'>"
                + "<strong style='color:#c8a0f0;'>FACTURA DE VENTA</strong> - "
                + "Emitida por la aplicacion <strong style='color:#c8a0f0;'>Blashape</strong> | "
                + COMPANY_NAME + " | " + COMPANY_NIT + " | " + COMPANY_ADDRESS + "<br/>"
                + "Este documento es generado automaticamente y tiene validez como comprobante de pago."
                + "</div>"

                + "</body></html>";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HTML — RECIBO DE PAGO (visualmente mejorado)
    // ─────────────────────────────────────────────────────────────────────────
    private String buildReceiptHtml(String customerName, String email,
                                    String invoiceId, long amount,
                                    String productName, String paymentTypeLabel,
                                    String subscriptionHtml, String date) {
        String formattedAmount = formatCOP(amount);

        return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8"/>
        <style>
          * { margin:0; padding:0; }
          body { font-family:Arial,sans-serif; font-size:12px; color:#2d2d2d; background:#ffffff; }
          .info-box { border:1px solid #e5d5fa; margin:0; }
          .info-box-header { background-color:#9117e4; color:#ffffff; font-size:10px;
                             font-weight:bold; letter-spacing:1px;
                             text-transform:uppercase; padding:6px 12px; }
          .data-table { width:100%%; border-collapse:collapse; }
          .data-table thead tr { background-color:#9117e4; }
          .data-table thead th { padding:9px 12px; font-size:11px;
                                 font-weight:bold; color:#ffffff; }
          .data-table tbody tr { border-bottom:1px solid #f0e5fc; }
          .badge { background-color:#f5eeff; border-left:4px solid #9117e4;
                   padding:8px 16px; font-size:12px; color:#555555; margin:10px 20px; }
          .footer { background-color:#1a1a2e; color:#aaaaaa; text-align:center;
                    padding:14px 20px; font-size:10px; line-height:1.8; margin-top:14px; }
        </style>
        </head>
        <body>

          <!-- HEADER -->
          <table style="width:100%%; background-color:#9117e4;" cellpadding="0" cellspacing="0">
            <tr>
              <td style="padding:16px 20px; vertical-align:middle;">
                <img src="%s" style="height:44px;"/>
              </td>
              <td style="padding:16px 20px; text-align:right; vertical-align:middle;">
                <div style="color:#ffffff; font-size:20px; font-weight:bold; letter-spacing:3px;">RECIBO DE PAGO</div>
                <div style="color:#ffffff; font-size:11px; margin-top:3px;">Fecha: %s</div>
              </td>
            </tr>
          </table>

          <!-- BADGE -->
          <div class="badge">
            Recibo N&#176;: <span style="color:#9117e4; font-weight:bold; font-size:13px;">%s</span>
          </div>

          <!-- INFO CLIENTE -->
          <table style="width:100%%; padding:0 20px; margin-bottom:12px;">
            <tr>
              <td style="padding:0 20px;">
                <div class="info-box">
                  <div class="info-box-header">Informacion del cliente</div>
                  <div style="padding:10px 12px;">
                    <p style="margin:3px 0; color:#444;"><strong>Cliente:</strong> %s</p>
                    <p style="margin:3px 0; color:#444;"><strong>Email:</strong> %s</p>
                    <p style="margin:3px 0; color:#444;"><strong>Tipo de pago:</strong> %s</p>
                    %s
                  </div>
                </div>
              </td>
            </tr>
          </table>

          <!-- TABLA -->
          <div style="margin:0 20px;">
            <table class="data-table">
              <thead>
                <tr>
                  <th style="text-align:left;">Descripcion</th>
                  <th style="text-align:right;">Precio</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td style="padding:10px 12px;">%s</td>
                  <td style="padding:10px 12px; text-align:right;">%s</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- TOTAL -->
          <table style="width:calc(100%% - 40px); margin:10px 20px 0 20px; background-color:#9117e4; border-radius:6px;">
            <tr>
              <td style="padding:12px 18px; color:#ffffff; font-size:13px;">Total a pagar</td>
              <td style="padding:12px 18px; color:#ffffff; font-size:16px; font-weight:bold; text-align:right;">%s</td>
            </tr>
          </table>

          <!-- FOOTER -->
          <div class="footer">
            <strong style="color:#c8a0f0;">Blashape</strong> - Recibo generado automaticamente por la aplicacion.<br/>
            Este documento no constituye una factura de venta.
          </div>

        </body>
        </html>
        """,
                LOGO_URL, date,
                invoiceId,
                customerName, email, paymentTypeLabel, subscriptionHtml,
                productName, formattedAmount,
                formattedAmount
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILIDADES
    // ─────────────────────────────────────────────────────────────────────────
    private byte[] renderToPdf(String html) throws Exception {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String formatCOP(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("es", "CO")).format(amount);
    }

    private String nvl(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }
}
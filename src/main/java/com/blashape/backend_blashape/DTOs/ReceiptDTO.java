package com.blashape.backend_blashape.DTOs;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptDTO {
    private String customerName;
    private String customerEmail;
    private String receiptId;        // ej: "INV-999"
    private String date;             // ej: "2026-04-21" (yyyy-MM-dd)
    private String productName;      // ej: "Plan Premium" o nombre del producto
    private long amount;             // en pesos, ej: 150000
    private String paymentType;      // "ONE_TIME_PRODUCT" o "SUBSCRIPTION"
    private String periodStart;      // solo si es suscripción, ej: "01/04/2026"
    private String periodEnd;        // solo si es suscripción, ej: "30/04/2026"
}

package com.blashape.backend_blashape.DTOs;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDTO {

    // Datos del cliente (Carpenter)
    private String customerName;       // name + lastName
    private String customerDni;        // dni
    private String customerEmail;      // email

    // Datos del taller del carpintero
    private String workshopName;       // Workshop.name
    private String workshopAddress;    // Workshop.address

    // Servicio
    private String concept;
    private Instant periodStart;
    private Instant periodEnd;
    private long unitCost;
    private double discountPercent;
    private double vatPercent;
    private String paymentMethod;

    // Metadatos
    private String invoiceId;
    private Instant invoiceDate;
}
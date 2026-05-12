package com.blashape.backend_blashape.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PqrsCreateResponse {
    private PqrsDTO pqrs;
    private String trackingCode;     // visible al usuario (ej. "PQRS-A3F7B2C9")
    private String trackingLink;     // magic link enviado por correo
    private boolean linkedToAccount; // true si se vinculó a un Customer existente
}
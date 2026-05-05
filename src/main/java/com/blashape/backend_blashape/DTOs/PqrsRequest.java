package com.blashape.backend_blashape.DTOs;

import com.blashape.backend_blashape.entitys.PqrsType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PqrsRequest {
    private String subject;
    private String message;
    private PqrsType type;
    private Long carpenterId;

    // Solo se usan si no hay sesión activa. Si hay sesión, se ignoran.
    private String guestName;
    private String guestLastName;
    private String guestEmail;
    private String guestPhone;
}
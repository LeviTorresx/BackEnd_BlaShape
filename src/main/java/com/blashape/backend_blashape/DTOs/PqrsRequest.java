package com.blashape.backend_blashape.DTOs;

import com.blashape.backend_blashape.entitys.PqrsScope;
import com.blashape.backend_blashape.entitys.PqrsType;
import lombok.AllArgsConstructor; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PqrsRequest {
    private String subject;
    private String message;
    private PqrsType type;
    private PqrsScope scope;       // GENERAL | WORKSHOP (default WORKSHOP)
    private Long workshopId;       // requerido si scope=WORKSHOP
    private Long carpenterId;      // legacy: si viene, se prioriza WORKSHOP por carpintero
    private String guestName;
    private String guestLastName;
    private String guestEmail;
    private String guestPhone;
}
package com.blashape.backend_blashape.DTOs;

import com.blashape.backend_blashape.entitys.PqrsScope;
import com.blashape.backend_blashape.entitys.PqrsStatus;
import com.blashape.backend_blashape.entitys.PqrsType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PqrsDTO {
    private Long pqrsId;
    private String subject;
    private String message;
    private PqrsType type;
    private PqrsStatus status;
    private PqrsScope scope;
    private String response;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime respondedAt;
    private Long customerId;
    private Long carpenterId;
    private Long workshopId;
    private String guestName;
    private String guestLastName;
    private String guestEmail;
    private String trackingCode;
    private boolean linkedToAccount;
}
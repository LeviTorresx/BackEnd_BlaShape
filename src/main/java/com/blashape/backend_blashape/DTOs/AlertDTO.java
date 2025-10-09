package com.blashape.backend_blashape.DTOs;

import com.blashape.backend_blashape.entitys.Severity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {
    Long alertId;
    String message;
    LocalDate date;
    LocalTime time;
    Severity severity;
}

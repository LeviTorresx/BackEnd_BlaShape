package com.blashape.backend_blashape.mapper;


import com.blashape.backend_blashape.DTOs.AlertDTO;
import com.blashape.backend_blashape.entitys.Alert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface AlertMapper {
    @Mapping(target = "date", expression = "java(convertToDate(dto.getDate()))")
    @Mapping(target = "time", expression = "java(convertToTime(dto.getTime()))")
    Alert toEntity(AlertDTO dto);

    @Mapping(target = "date", expression = "java(formatDate(alert.getDate()))")
    @Mapping(target = "time", expression = "java(formatTime(alert.getTime()))")
    AlertDTO toDTO(Alert alert);

    default LocalDate convertToDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    default LocalTime convertToTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    default String formatDate(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
    }

    default String formatTime(LocalTime time) {
        return time != null ? time.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : null;
    }
}

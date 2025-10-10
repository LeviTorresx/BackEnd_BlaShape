package com.blashape.backend_blashape.mapper;


import com.blashape.backend_blashape.DTOs.AlertDTO;
import com.blashape.backend_blashape.entitys.Alert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface AlertMapper {
    @Mapping(target = "carpenter", ignore = true)
    @Mapping(target = "date", source = "date", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "time", source = "time", qualifiedByName = "stringToLocalTime")
    Alert toEntity(AlertDTO dto);

    @Mapping(target = "carpenterId", source = "carpenter.carpenterId")
    @Mapping(source = "date", target = "date", qualifiedByName = "localDateToString")
    @Mapping(source = "time", target = "time", qualifiedByName = "localTimeToString")
    AlertDTO toDTO(Alert alert);

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String date) {
        if (date == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, formatter);
    }

    @Named("stringToLocalTime")
    default LocalTime stringToLocalTime(String time) {
        if (time == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return LocalTime.parse(time, formatter);
    }

    @Named("localDateToString")
    default String localDateToString(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
    }

    @Named("localTimeToString")
    default String localTimeToString(LocalTime time) {
        return time != null ? time.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : null;
    }
}

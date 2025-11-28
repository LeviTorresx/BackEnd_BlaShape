package com.blashape.backend_blashape.mapper;


import com.blashape.backend_blashape.DTOs.FurnitureDTO;
import com.blashape.backend_blashape.DTOs.RequestFurniture;
import com.blashape.backend_blashape.entitys.Furniture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;

@Mapper (componentModel = "spring", uses= {CuttingMapper.class})
public interface FurnitureMapper {

    @Mapping(target = "carpenterId",source = "carpenter.carpenterId")
    @Mapping(target = "customerId", source = "customer.customerId")
    FurnitureDTO toDTO(Furniture furniture);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "carpenter", ignore = true)
    @Mapping(target = "cutting.furniture", ignore = true)
    Furniture toEntity(FurnitureDTO dto);

    // --- DTO → Request ---
    @Mapping(target = "creationDate", expression = "java(toStringDate(dto.getCreationDate()))")
    @Mapping(target = "endDate", expression = "java(toStringDate(dto.getEndDate()))")
    RequestFurniture toRequest(FurnitureDTO dto);

    // --- Request → DTO ---
    @Mapping(target = "creationDate", expression = "java(toLocalDate(req.getCreationDate()))")
    @Mapping(target = "endDate", expression = "java(toLocalDate(req.getEndDate()))")
    FurnitureDTO toDto(RequestFurniture req);

    // -----------------------------
    // Métodos auxiliares
    // -----------------------------

    default String toStringDate(LocalDate date) {
        return date == null ? null : date.toString();
    }

    default LocalDate toLocalDate(String dateStr) {
        return (dateStr == null || dateStr.isBlank()) ? null : LocalDate.parse(dateStr);
    }
}

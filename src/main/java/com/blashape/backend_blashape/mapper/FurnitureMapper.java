package com.blashape.backend_blashape.mapper;

import com.blashape.backend_blashape.DTOs.FurnitureDTO;
import com.blashape.backend_blashape.DTOs.RequestFurniture;
import com.blashape.backend_blashape.entitys.Furniture;
import org.mapstruct.*;

import java.time.LocalDate;

@Mapper(componentModel = "spring", uses = {CuttingMapper.class})
public interface FurnitureMapper {

    // ================= ENTITY -> DTO =================
    @Mapping(target = "carpenterId", source = "carpenter.carpenterId")
    @Mapping(target = "customerId", source = "customer.customerId")
    FurnitureDTO toDTO(Furniture furniture);


    // ================= DTO -> ENTITY =================
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "carpenter", ignore = true)
    @Mapping(target = "cutting.furniture", ignore = true)
    Furniture toEntity(FurnitureDTO dto);


    // ================= REQUEST -> ENTITY =================
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "carpenter", ignore = true)
    @Mapping(target = "imageInitURL", ignore = true)
    @Mapping(target = "imageEndURL", ignore = true)
    @Mapping(target = "documentURL", ignore = true)
    @Mapping(target = "creationDate", expression = "java(toLocalDate(request.getCreationDate()))")
    @Mapping(target = "endDate", expression = "java(toLocalDate(request.getEndDate()))")
    @Mapping(target = "cutting.furniture", ignore = true)
    Furniture toEntity(RequestFurniture request);


    // ================= REQUEST -> DTO =================
    @Mapping(target = "documentURL", ignore = true)
    @Mapping(target = "imageInitURL", ignore = true)
    @Mapping(target = "imageEndURL", ignore = true)
    @Mapping(target = "creationDate", expression = "java(toLocalDate(request.getCreationDate()))")
    @Mapping(target = "endDate", expression = "java(toLocalDate(request.getEndDate()))")
    FurnitureDTO toDTO(RequestFurniture request);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "carpenter", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "cutting", ignore = true) // 🔥 importante: cutting lo manejas en el service
    void updateEntityFromRequest(RequestFurniture request, @MappingTarget Furniture entity);


    default LocalDate toLocalDate(String dateStr) {
        return (dateStr == null || dateStr.isBlank()) ? null : LocalDate.parse(dateStr);
    }
}
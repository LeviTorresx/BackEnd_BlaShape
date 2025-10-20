package com.blashape.backend_blashape.mapper;


import com.blashape.backend_blashape.DTOs.FurnitureDTO;
import com.blashape.backend_blashape.entitys.Furniture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper (componentModel = "spring")
public interface FurnitureMapper {
    @Mapping(target = "carpenterId",source = "carpenter.carpenterId")
    FurnitureDTO toDTO(Furniture furniture);

    @Mapping(target = "carpenter", ignore = true)
    Furniture toEntity(FurnitureDTO dto);
}

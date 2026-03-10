package com.blashape.backend_blashape.mapper;

import com.blashape.backend_blashape.DTOs.CuttingDTO;
import com.blashape.backend_blashape.entitys.Cutting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PieceMapper.class})
public interface CuttingMapper {

    // ENTITY → DTO
    @Mapping(target = "furnitureId", source = "furniture.furnitureId")
    CuttingDTO toDTO(Cutting cutting);

    // DTO → ENTITY
    @Mapping(target = "furniture", ignore = true) // Se setea en el servicio
    @Mapping(target = "pieces", ignore = true)    // Se setean en el servicio
    Cutting toEntity(CuttingDTO dto);
}

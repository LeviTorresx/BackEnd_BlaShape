package com.blashape.backend_blashape.mapper;

import com.blashape.backend_blashape.DTOs.CarpenterDTO;
import com.blashape.backend_blashape.entitys.Carpenter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarpenterMapper {
    @Mapping(source = "workshop.workshopId", target = "workshopId")
    CarpenterDTO toDTO(Carpenter carpenter);

    @Mapping(source = "workshopId", target = "workshop.workshopId")
    Carpenter toEntity(CarpenterDTO dto);
}

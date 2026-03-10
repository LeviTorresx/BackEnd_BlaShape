package com.blashape.backend_blashape.mapper;

import com.blashape.backend_blashape.DTOs.CarpenterDTO;
import com.blashape.backend_blashape.entitys.Carpenter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {WorkshopMapper.class})
public interface CarpenterMapper {

    @Mapping(source = "workshop", target = "workshop")
    CarpenterDTO toDTO(Carpenter carpenter);

    @Mapping(source = "workshop", target = "workshop")
    Carpenter toEntity(CarpenterDTO dto);
}
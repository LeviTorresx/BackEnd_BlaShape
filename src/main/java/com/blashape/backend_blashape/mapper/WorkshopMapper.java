package com.blashape.backend_blashape.mapper;

import com.blashape.backend_blashape.DTOs.WorkshopDTO;
import com.blashape.backend_blashape.entitys.Workshop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkshopMapper {

    @Mapping(target = "carpenterId", source = "carpenter.carpenterId")
    WorkshopDTO toDto(Workshop workshop);

    @Mapping(target = "carpenter", ignore = true)
    Workshop toEntity(WorkshopDTO dto);
}


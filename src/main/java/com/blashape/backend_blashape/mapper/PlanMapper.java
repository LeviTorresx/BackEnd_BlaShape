package com.blashape.backend_blashape.mapper;

import org.mapstruct.Mapper;

import com.blashape.backend_blashape.DTOs.PlanDTO;
import com.blashape.backend_blashape.entitys.Plan;

@Mapper(componentModel = "spring")
public interface PlanMapper {
    Plan toEntity(PlanDTO dto);

    PlanDTO toDTO(Plan plan);
}

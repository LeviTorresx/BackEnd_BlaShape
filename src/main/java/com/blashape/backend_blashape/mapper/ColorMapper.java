package com.blashape.backend_blashape.mapper;

import com.blashape.backend_blashape.DTOs.ColorDTO;
import com.blashape.backend_blashape.entitys.Color;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ColorMapper {

    ColorDTO toDTO(Color color);

    Color toEntity(ColorDTO dto);
}

package com.blashape.backend_blashape.mapper;

import com.blashape.backend_blashape.DTOs.EdgesDTO;
import com.blashape.backend_blashape.entitys.Edges;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EdgesMapper {
    EdgesDTO toDTO(Edges edges);

    Edges toEntity(EdgesDTO dto);
}

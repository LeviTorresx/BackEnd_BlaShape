package com.blashape.backend_blashape.mapper;

import com.blashape.backend_blashape.DTOs.PieceDTO;
import com.blashape.backend_blashape.entitys.Piece;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {EdgesMapper.class, ColorMapper.class})
public interface PieceMapper {
    PieceDTO toDTO(Piece piece);

    Piece toEntity(PieceDTO dto);

    @Mapping(target = "cutting", ignore = true) // no tocar cutting aquí
    void updateEntityFromDTO(PieceDTO dto, @MappingTarget Piece entity);
}

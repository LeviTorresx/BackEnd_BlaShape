package com.blashape.backend_blashape.mapper;

import com.blashape.backend_blashape.DTOs.PieceDTO;
import com.blashape.backend_blashape.entitys.Piece;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {EdgesMapper.class, ColorMapper.class})
public interface PieceMapper {

    @Mapping(source = "color", target = "colorDTO")
    @Mapping(source = "edges", target = "edgesDTO")
    PieceDTO toDTO(Piece piece);

    @Mapping(target = "cutting", ignore = true)
    @Mapping(target = "pieceId", ignore = true) // ← ignorar ID al crear
    @Mapping(source = "colorDTO", target = "color")
    @Mapping(source = "edgesDTO", target = "edges")
    Piece toEntity(PieceDTO dto);

    @Mapping(target = "cutting", ignore = true)
    @Mapping(target = "pieceId", ignore = true) // ← ignorar ID al actualizar
    @Mapping(source = "colorDTO", target = "color")
    @Mapping(source = "edgesDTO", target = "edges")
    void updateEntityFromDTO(PieceDTO dto, @MappingTarget Piece entity);
}

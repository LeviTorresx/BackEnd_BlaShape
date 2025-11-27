package com.blashape.backend_blashape.mapper;

import com.blashape.backend_blashape.DTOs.PieceDTO;
import com.blashape.backend_blashape.entitys.Piece;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {EdgesMapper.class})
public interface PieceMapper {
    PieceDTO toDTO(Piece piece);

    Piece toEntity(PieceDTO dto);
}

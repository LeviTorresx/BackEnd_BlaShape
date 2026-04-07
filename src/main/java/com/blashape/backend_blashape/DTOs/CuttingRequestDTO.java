package com.blashape.backend_blashape.DTOs;

import com.blashape.backend_blashape.entitys.Edges;
import com.blashape.backend_blashape.entitys.Material;
import com.blashape.backend_blashape.entitys.Piece;
import com.blashape.backend_blashape.entitys.Sheet;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CuttingRequestDTO {
    private String proyect;

    @NotNull(message = "El tablero es obligatorio")
    @Valid
    private SheetDTO sheet;

    @NotEmpty(message = "Debe incluir al menos una pieza")
    @Valid
    private List<PieceDTO> pieces;
}

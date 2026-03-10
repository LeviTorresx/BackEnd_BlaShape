package com.blashape.backend_blashape.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CuttingDTO {
    private Long cuttingId;
    private Long furnitureId;
    private String materialName;
    private int sheetQuantity;
    private List<PieceDTO> pieces;
}


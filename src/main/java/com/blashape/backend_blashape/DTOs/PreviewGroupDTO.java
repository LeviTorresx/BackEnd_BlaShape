package com.blashape.backend_blashape.DTOs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class PreviewGroupDTO {
    @NotNull
    @Valid
    private SheetDTO sheet;

    @NotEmpty
    @Valid
    private List<PieceDTO> pieces;
}

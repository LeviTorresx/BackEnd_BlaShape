package com.blashape.backend_blashape.DTOs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class PreviewRequestDTO {

    @NotEmpty(message = "Debe incluir al menos un grupo")
    @Valid
    private List<PreviewGroupDTO> groups;
}

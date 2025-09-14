package com.blashape.backend_blashape.DTOs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDTO {
    private Long materialId;
    private String colorName;
    private String colorHex;
    private List<Double> thickness;
    private String name;
}

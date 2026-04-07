package com.blashape.backend_blashape.DTOs;
import com.blashape.backend_blashape.entitys.Color;
import com.blashape.backend_blashape.entitys.Material;
import com.blashape.backend_blashape.entitys.Sheet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SheetDTO {
    private Long sheetId;
    private double height;
    private double width;
    private MaterialDTO materialDTO;

    public Sheet toModel() {
        Sheet sheet = new Sheet();
        sheet.setWidth(width);
        sheet.setHeight(height);

        Material mat = new Material();
        mat.setName(materialDTO.getName());

        Color color = new Color();
        color.setName(materialDTO.getColor().getName());
        color.setHex(materialDTO.getColor().getHex());

        mat.setColor(color);

        mat.setThickness(materialDTO.getThickness());

        sheet.setMaterial(mat);

        return sheet;
    }
}

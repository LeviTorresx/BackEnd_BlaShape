package com.blashape.backend_blashape.DTOs;

import com.blashape.backend_blashape.entitys.CuttingSheet;
import com.blashape.backend_blashape.entitys.Sheet;
import lombok.Getter;

@Getter
public class SheetPreviewDTO {

    private final int    planeNumber;
    private final String svgContent;
    private final double percentageUtilized;
    private final int    totalPieces;

    // Info del tablero
    private final double sheetWidth;
    private final double sheetHeight;
    private final String materialName;
    private final double materialThickness;
    private final String colorName;
    private final String colorHex;

    public SheetPreviewDTO(CuttingSheet cuttingSheet, String svg) {
        Sheet s = cuttingSheet.getSheet();

        this.planeNumber        = cuttingSheet.getPlaneNumber();
        this.svgContent         = svg;
        this.percentageUtilized = cuttingSheet.getPercentageUtilized();
        this.totalPieces        = cuttingSheet.getCuts().size();

        this.sheetWidth         = s.getWidth();
        this.sheetHeight        = s.getHeight();
        this.materialName       = s.getMaterial() != null ? s.getMaterial().getName() : null;
        this.materialThickness  = s.getMaterial() != null ? s.getMaterial().getThickness() : 0;
        this.colorName          = s.getMaterial() != null && s.getMaterial().getColor() != null
                ? s.getMaterial().getColor().getName() : null;
        this.colorHex           = s.getMaterial() != null && s.getMaterial().getColor() != null
                ? s.getMaterial().getColor().getHex() : null;
    }
}
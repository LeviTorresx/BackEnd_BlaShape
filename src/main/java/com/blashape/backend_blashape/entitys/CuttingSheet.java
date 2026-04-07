package com.blashape.backend_blashape.entitys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuttingSheet {
    private Sheet sheet;
    private List<CuttingPosition> cuts = new ArrayList<>();
    private int planeNumber;

    public CuttingSheet(Sheet sheet, int planeNumber) {
        this.sheet = sheet;
        this.planeNumber = planeNumber;
    }

    public double getAreaUtilizedMm2(){
        return cuts.stream()
                .mapToDouble(c -> c.getEffectiveHeight() * c.getEffectiveWidth()).sum();
    }

    public double getPercentageUtilized(){
        double totalArea = sheet.getHeight() * sheet.getWidth();
        if(totalArea == 0) return 0;

        return (getAreaUtilizedMm2() / totalArea) * 100;
    }

    public double getWastedM2(){
        double areaUtilized = sheet.getHeight()*sheet.getWidth();
        return (areaUtilized -  getAreaUtilizedMm2()) /1_000_000.0;
    }
}

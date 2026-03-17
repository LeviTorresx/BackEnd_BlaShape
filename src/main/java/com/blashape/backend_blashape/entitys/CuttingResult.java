package com.blashape.backend_blashape.entitys;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CuttingResult {
    private List<CuttingSheet> sheets;
    private int usedSheets;
    private double averageUtilizationPercentage;
    private double totalWastedM2;
    private double totalMlBanding;
    private int totalPiecesLocated;
    private int MissingParts;
    private long timeMs;
}

package com.blashape.backend_blashape.DTOs;

import com.blashape.backend_blashape.entitys.CuttingPosition;
import com.blashape.backend_blashape.entitys.CuttingResult;
import com.blashape.backend_blashape.entitys.CuttingSheet;
import com.blashape.backend_blashape.services.BandingService;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CuttingResponseDTO {

    private int    sheetUsed;
    private double utilization;   // porcentaje promedio 0-100
    private double wastedM2;
    private int    locatedPieces;
    private int    missingPieces;
    private long   timeMs;

    private BandingDTO banding;
    private List<SheetDTO> sheets;

    // ─── Tapacanto ──────────────────────────────────────────────────────────────

    @Data
    public static class BandingDTO {
        private double totaMlWidth;
        private double totalMlHeight;
        private double totalMlGeneral;
        private Map<String, Double> toMlPiece;

        public static BandingDTO from( BandingService.BandingSummary r) {
            BandingDTO dto = new BandingDTO();
            dto.totaMlWidth   = round2(r.totalMlWidth());
            dto.totalMlHeight  = round2(r.totalMlHeight());
            dto.totalMlGeneral = round2(r.totalMlGeneral());
            dto.toMlPiece = r.toMlPiece().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> round2(e.getValue())));
            return dto;
        }
    }

    // ─── Plano ──────────────────────────────────────────────────────────────────

    @Data
    public static class SheetDTO {
        private int    planeNumber;
        private double percentageUtilized;
        private double wastedM2;
        private int    sizeCuts;
        private List<CuttingPositionDTO> cuts;

        public static SheetDTO from(CuttingSheet p) {
            SheetDTO dto = new SheetDTO();
            dto.planeNumber          = p.getPlaneNumber();
            dto.percentageUtilized = round2(p.getPercentageUtilized());
            dto.wastedM2   = round2(p.getWastedM2());
            dto.sizeCuts  = p.getCuts().size();
            dto.cuts          = p.getCuts().stream()
                    .map(CuttingPositionDTO::from)
                    .collect(Collectors.toList());
            return dto;
        }
    }

    // ─── Corte individual ───────────────────────────────────────────────────────

    @Data
    public static class CuttingPositionDTO {
        private String  name;
        private double  x;
        private double  y;
        private double  effectiveWidth;
        private double  effectiveHeight;
        private boolean rotada;

        public static CuttingPositionDTO from(CuttingPosition c) {
            CuttingPositionDTO dto = new CuttingPositionDTO();
            dto.name  = c.getPiece().getName();
            dto.x      = c.getX();
            dto.y      = c.getY();
            dto.effectiveWidth  = c.getEffectiveWidth();
            dto.effectiveHeight  = c.getEffectiveHeight();
            dto.rotada = c.isRotated();
            return dto;
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // ─── Factory ────────────────────────────────────────────────────────────────

    public static CuttingResponseDTO from(CuttingResult r,
                                          BandingService.BandingSummary t) {
        CuttingResponseDTO dto = new CuttingResponseDTO();
        dto.sheetUsed  = r.getTotalPiecesLocated();
        dto.utilization = round2(r.getAverageUtilizationPercentage());
        dto.wastedM2   = round2(r.getTotalWastedM2());
        dto.locatedPieces  = r.getTotalPiecesLocated();
        dto.missingPieces = r.getMissingParts();
        dto.timeMs        = r.getTimeMs();
        dto.banding       = BandingDTO.from(t);
        dto.sheets          = r.getSheets().stream()
                .map(SheetDTO::from)
                .collect(Collectors.toList());
        return dto;
    }

   
}

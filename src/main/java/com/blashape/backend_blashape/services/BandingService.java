package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.entitys.CuttingPosition;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BandingService {
    public record BandingSummary(
            double totalMlWidth,    // metros lineales sobre ejes X (caraX1 + caraX2)
            double totalMlHeight,    // metros lineales sobre ejes Y (caraY1 + caraY2)
            double totalMlGeneral,
            Map<String, Double> toMlPiece
    ) {}

    public BandingSummary calcular(List<CuttingPosition> cuts) {
        double mlWidth = 0;
        double mlHeight = 0;
        Map<String, Double> perPiece = new LinkedHashMap<>();

        for (CuttingPosition c : cuts) {
            double mlPiece    = 0;
            double widthEffect = c.getEffectiveWidth() / 1000.0;  // mm → m
            double heightEffect = c.getEffectiveHeight() / 1000.0;

            // Caras paralelas al eje X (longitud = anchoEfectivo)
            if (c.isEdgeBandingX1()) { mlWidth += widthEffect; mlPiece += widthEffect; }
            if (c.isEdgeBandingX2()) { mlWidth += widthEffect; mlPiece += widthEffect; }

            // Caras paralelas al eje Y (longitud = largoEfectivo)
            if (c.isEdgeBandingY1()) { mlHeight += heightEffect; mlPiece += heightEffect; }
            if (c.isEdgeBandingY2()) { mlHeight += heightEffect; mlPiece += heightEffect; }

            String name = c.getPiece().getName();
            perPiece.merge(name, mlPiece, Double::sum);
        }

        return new BandingSummary(mlWidth, mlHeight, mlWidth + mlHeight, perPiece);
    }
}

package com.blashape.backend_blashape.services;


import com.blashape.backend_blashape.entitys.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class GuillotineAlgorithm {

    // Tolerancia para evitar espacios de menos de 1 mm (polvo de sierra)
    private static final double TOLERANCE_MM = 1.0;

    record FreeSpace(double x, double y, double width, double height) {
        boolean fit(double w, double h) {
            return w <= width && h <= height;
        }
    }

    private Optional<CuttingPosition> searchSpace(Piece piece,
                                                  List<FreeSpace> spaces) {
        for (FreeSpace esp : spaces) {
            // Orientación original
            if (esp.fit(piece.getWidth(), piece.getHeight())) {
                return Optional.of(new CuttingPosition(piece, esp.x(), esp.y(), false));
            }
            // Rotación 90°
            if (piece.getRotationAllowed() &&
                    esp.fit(piece.getWidth(), piece.getHeight())) {
                return Optional.of(new CuttingPosition(piece, esp.x(), esp.y(), true));
            }
        }
        return Optional.empty();
    }

    private void updateSpaces (List<FreeSpace> spaces,
                                    CuttingPosition cut) {
        // Encontrar el espacio que contiene el corte
        FreeSpace used = spaces.stream()
                .filter(e -> e.x() == cut.getX() && e.y() == cut.getY())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Espacio no encontrado para corte en (" +
                                cut.getX() + "," + cut.getY() + ")"));

        spaces.remove(used);

        double pw = cut.getEffectiveWidth();
        double ph = cut.getEffectiveHeight();

        // Espacio a la derecha de la pieza
        double rightWidthSpa = used.width - pw;
        if ( rightWidthSpa> TOLERANCE_MM) {
            spaces.add(new FreeSpace(
                    cut.getX() + pw,
                    cut.getY(),
                    rightWidthSpa,
                    ph));
        }

        // Espacio inferior (ocupa todo el ancho del espacio original)
        double heightBottomSpa = used.height - ph;
        if (heightBottomSpa > TOLERANCE_MM) {
            spaces.add(new FreeSpace(
                    cut.getX(),
                    cut.getY() + ph,
                    used.width,
                    heightBottomSpa));
        }

        // Best-Fit: espacios más pequeños primero
        spaces.sort(Comparator.comparingDouble(e -> e.width * e.height));
    }

    private CuttingResult buildResults(List<CuttingSheet> sheets,
                                                     int wasLocated, long timeMs) {
        int located = sheets.stream()
                .mapToInt(p -> p.getCuts().size())
                .sum();

        double avrUtilization = sheets.stream()
                .mapToDouble(CuttingSheet::getPercentageUtilized)
                .average()
                .orElse(0);

        double totalWaste = sheets.stream()
                .mapToDouble(CuttingSheet::getWastedM2)
                .sum();

        CuttingResult r = new CuttingResult();
        r.setSheets(sheets);
        r.setUsedSheets(sheets.size());
        r.setAverageUtilizationPercentage(avrUtilization);
        r.setTotalWastedM2(totalWaste);
        r.setTotalPiecesLocated(located);
        r.setMissingParts(wasLocated);
        r.setTimeMs(timeMs);
        return r;
    }

    public CuttingResult optimize(List<Piece> pieces, Sheet sheet) {
        long init = System.currentTimeMillis();

        List<Piece> queue = pieces.stream()
                .flatMap(piece -> Collections.nCopies(piece.getQuantity(), piece).stream())
                .sorted(Comparator.comparingDouble(Piece::getAreaMm2).reversed())
                .collect(Collectors.toCollection(ArrayList::new));

        List<CuttingSheet> sheets = new ArrayList<>();
        int numSheets = 1;

        while (!queue.isEmpty()) {
            CuttingSheet cuttingSheet = new CuttingSheet(sheet, numSheets++);
            List<FreeSpace> spaces = new ArrayList<>();
            spaces.add(new FreeSpace(0, 0, sheet.getWidth(), sheet.getHeight()));

            boolean wasLocated = false;
            Iterator<Piece> it = queue.iterator();

            while (it.hasNext()) {
                Piece piece = it.next();
                Optional<CuttingPosition> pos = searchSpace(piece, spaces);
                if (pos.isPresent()) {
                    cuttingSheet.getCuts().add(pos.get());
                    updateSpaces(spaces, pos.get());
                    it.remove();
                    wasLocated = true;
                }
            }

            if (!wasLocated) {
                // Ninguna pieza restante cabe en un tablero nuevo
                break;
            }

            sheets.add(cuttingSheet);
        }
        return buildResults(sheets, queue.size(), System.currentTimeMillis() - init);
    }

}

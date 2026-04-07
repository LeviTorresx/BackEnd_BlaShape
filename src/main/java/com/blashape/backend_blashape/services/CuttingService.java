package com.blashape.backend_blashape.services;
import com.blashape.backend_blashape.DTOs.*;
import com.blashape.backend_blashape.entitys.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CuttingService {

    private final GuillotineAlgorithm guillotineAlgorithm;
    private final PreviewGeneratorSVG previewGeneratorSVG;

    public List<SheetPreviewDTO> generatePreviews(PreviewRequestDTO req) {

        RenderOptions opts = RenderOptions.resolveOptions(req.getPlan());
        List<SheetPreviewDTO> result = new ArrayList<>();

        for (PreviewGroupDTO group : req.getGroups()) {

            Sheet sheet = group.getSheet().toModel();
            List<Piece> pieces = group.getPieces().stream()
                    .map(PieceDTO::toModel)
                    .collect(Collectors.toList());

            CuttingResult cuttingResult = guillotineAlgorithm.optimize(pieces, sheet);

            for (CuttingSheet cuttingSheet : cuttingResult.getSheets()) {
                String svg = previewGeneratorSVG.generateSVG(cuttingSheet, opts);

                result.add(new SheetPreviewDTO(cuttingSheet, svg));
            }
        }

        result.sort((a, b) -> Integer.compare(a.getPlaneNumber(), b.getPlaneNumber()));
        return result;
    }

    public CuttingResult optimized(CuttingRequestDTO req) {
        Sheet sheet = req.getSheet().toModel();
        List<Piece> pieces = req.getPieces().stream()
                .map(PieceDTO::toModel)
                .collect(Collectors.toList());
        return guillotineAlgorithm.optimize(pieces, sheet);
    }

    public List<CuttingPosition> allCuts(CuttingResult r) {
        return r.getSheets().stream()
                .flatMap(p -> p.getCuts().stream())
                .collect(Collectors.toList());
    }

    public CuttingSheet getSheet(CuttingRequestDTO req, int index) {
        List<CuttingSheet> sheet = optimized(req).getSheets();
        if (index < 0 || index >= sheet.size()) {
            throw new IndexOutOfBoundsException(
                    "Indice " + index + " fuera de rango (total: " + sheet.size() + ")");
        }
        return sheet.get(index);
    }
}


package com.blashape.backend_blashape.controllers;

import com.blashape.backend_blashape.DTOs.CuttingRequestDTO;
import com.blashape.backend_blashape.entitys.*;
import com.blashape.backend_blashape.services.BandingService;
import com.blashape.backend_blashape.services.GuillotineAlgorithm;
import com.blashape.backend_blashape.services.PdfReportService;
import com.blashape.backend_blashape.services.PreviewGeneratorSVG;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api_BS/cutting")
@RequiredArgsConstructor
public class CuttingController {

    private final GuillotineAlgorithm guillotineAlgorithm;
    private final PreviewGeneratorSVG previewGeneratorSVG;
    private final BandingService bandingService;
    private final PdfReportService pdfReportService;

    @PostMapping(value = "/sheet/{index}/svg", produces = "image/svg+xml")
    public ResponseEntity<String> getSvg(
            @RequestBody @Valid CuttingRequestDTO req,
            @PathVariable int index) {

        CuttingSheet sheet = getSheet(req, index);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .body(previewGeneratorSVG.generateSVG(sheet));
    }

    @PostMapping(value = "/cutting_plan/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> getPdf(
            @RequestBody @Valid CuttingRequestDTO req) throws Exception {

        CuttingResult result = optimized(req);
        List<CuttingPosition> all = allCuts(result);
        BandingService.BandingSummary banding =  bandingService.calcular(all);
        result.setTotalMlBanding(banding.totalMlGeneral());

        byte[] pdfBytes = pdfReportService.generate(result, banding, req.getProyect());

        String filename = req.getProyect() != null && !req.getProyect().isBlank()
                ? req.getProyect().replaceAll("[^a-zA-Z0-9_-]", "_") + ".pdf"
                : "plan_de_corte.pdf";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }


    // ─── Helpers ───────────────────────────────────────────────────────────────

    private CuttingResult optimized(CuttingRequestDTO req) {
        Sheet sheet = req.getSheet().toModel();
        List<Piece> pieces = req.getPieces().stream()
                .map(CuttingRequestDTO.PieceDTO::toModel)
                .collect(Collectors.toList());
        return guillotineAlgorithm.optimize(pieces, sheet);
    }

    private List<CuttingPosition> allCuts(CuttingResult r) {
        return r.getSheets().stream()
                .flatMap(p -> p.getCuts().stream())
                .collect(Collectors.toList());
    }

    private CuttingSheet getSheet(CuttingRequestDTO req, int index) {
        List<CuttingSheet> sheet = optimized(req).getSheets();
        if (index < 0 || index >= sheet.size()) {
            throw new IndexOutOfBoundsException(
                    "Indice " + index + " fuera de rango (total: " + sheet.size() + ")");
        }
        return sheet.get(index);
    }

}

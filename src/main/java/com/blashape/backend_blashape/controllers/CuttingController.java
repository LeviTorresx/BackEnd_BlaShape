package com.blashape.backend_blashape.controllers;

import com.blashape.backend_blashape.DTOs.CuttingRequestDTO;
import com.blashape.backend_blashape.DTOs.PreviewRequestDTO;
import com.blashape.backend_blashape.DTOs.SheetPreviewDTO;
import com.blashape.backend_blashape.entitys.*;
import com.blashape.backend_blashape.services.*;
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

    private final BandingService bandingService;
    private final PdfReportService pdfReportService;
    private  final CuttingService cuttingService;

    @PostMapping(value = "/cutting_plan/preview_svgs")
    public ResponseEntity<List<SheetPreviewDTO>> generatePreviews(
            @Valid @RequestBody PreviewRequestDTO req) {
        String plan = "FREE";
        return ResponseEntity.ok(cuttingService.generatePreviews(req, plan));
    }

    // none
    @PostMapping(value = "/cutting_plan/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> getPdf(
            @RequestBody @Valid CuttingRequestDTO req) throws Exception {

        CuttingResult result = cuttingService.optimized(req);
        List<CuttingPosition> all = cuttingService.allCuts(result);
        BandingService.BandingSummary banding =  bandingService.calcular(all);
        result.setTotalMlBanding(banding.totalMlGeneral());

        RenderOptions opts = RenderOptions.proPlan();

        byte[] pdfBytes = pdfReportService.generate(result, banding, req.getProyect(), opts);

        String filename = req.getProyect() != null && !req.getProyect().isBlank()
                ? req.getProyect().replaceAll("[^a-zA-Z0-9_-]", "_") + ".pdf"
                : "plan_de_corte.pdf";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

}

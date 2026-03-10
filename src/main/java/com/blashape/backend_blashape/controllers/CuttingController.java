package com.blashape.backend_blashape.controllers;

import com.blashape.backend_blashape.DTOs.CuttingDTO;
import com.blashape.backend_blashape.DTOs.SvgRequest;
import com.blashape.backend_blashape.DTOs.SvgResponse;
import com.blashape.backend_blashape.services.CuttingService;
import com.blashape.backend_blashape.services.GuillotineAlgorithm;
import com.blashape.backend_blashape.services.SvgPreviewGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api_BS/cutting")
@RequiredArgsConstructor
public class CuttingController {
    private final CuttingService cuttingService;

    @PostMapping("/preview")
    public SvgResponse generatePreview(@RequestBody SvgRequest request) {
        return cuttingService.createCuttingPreview(request);
    }

    @GetMapping(value = "/preview/{sheet}", produces = "image/svg+xml")
    public String previewSheet(
            @PathVariable int sheet,
            @RequestBody SvgRequest request
    ) {

        GuillotineAlgorithm.PackingResult result = GuillotineAlgorithm.pack(
                request.getContainerWidth(),
                request.getContainerHeight(),
                request.getItems(),
                request.getKerf()
        );

        List<String> svgs = SvgPreviewGenerator.generateAll(
                result.sheets(),
                result.wastePercents(),
                request.getContainerWidth(),
                request.getContainerHeight(),
                request.getKerf(),
                request.getPreviewWidth(),
                request.getPreviewHeight()
        );

        return svgs.get(sheet);
    }
}

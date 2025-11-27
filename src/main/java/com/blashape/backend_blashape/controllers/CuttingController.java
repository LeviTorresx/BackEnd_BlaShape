package com.blashape.backend_blashape.controllers;

import com.blashape.backend_blashape.DTOs.CuttingDTO;
import com.blashape.backend_blashape.services.CuttingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api_BS/cutting")
@RequiredArgsConstructor
public class CuttingController {
    private final CuttingService cuttingService;

    @PostMapping("/create")
    public ResponseEntity<CuttingDTO> createOrUpdate(@RequestBody CuttingDTO dto) {
        return ResponseEntity.ok(cuttingService.saveCutting(dto));
    }

    @GetMapping("/furniture/{furnitureId}")
    public ResponseEntity<CuttingDTO> getByFurniture(@PathVariable Long furnitureId) {
        return ResponseEntity.ok(cuttingService.getByFurnitureId(furnitureId));
    }
}

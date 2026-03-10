package com.blashape.backend_blashape.controllers;

import com.blashape.backend_blashape.DTOs.CuttingDTO;
import com.blashape.backend_blashape.DTOs.FurnitureDTO;
import com.blashape.backend_blashape.services.CuttingService;
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

    @PostMapping("/create")
    public ResponseEntity<CuttingDTO> createCutting(@RequestBody CuttingDTO dto) {
        return ResponseEntity.ok(cuttingService.createCutting(dto));
    }

    @GetMapping("/furniture/{furnitureId}")
    public ResponseEntity<CuttingDTO> getByFurniture(@PathVariable Long furnitureId) {
        return ResponseEntity.ok(cuttingService.getByFurnitureId(furnitureId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CuttingDTO>> getAllByCarpenter(
            @CookieValue(name = "jwt", required = false) String token
    ) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<CuttingDTO> cuttings = cuttingService.getCuttingsByToken(token);
        return ResponseEntity.ok(cuttings);
    }

    @PutMapping("/edit/{cuttingId}")
    public ResponseEntity<CuttingDTO> updateCutting(@PathVariable Long cuttingId, @RequestBody CuttingDTO dto) {
        return ResponseEntity.ok(cuttingService.updateCutting(cuttingId, dto));
    }
}

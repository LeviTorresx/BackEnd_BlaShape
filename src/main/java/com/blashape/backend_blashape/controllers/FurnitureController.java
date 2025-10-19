package com.blashape.backend_blashape.controllers;

import com.blashape.backend_blashape.DTOs.AlertDTO;
import com.blashape.backend_blashape.DTOs.FurnitureDTO;
import com.blashape.backend_blashape.services.FurnitureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api_BS/furniture")
@RequiredArgsConstructor
public class FurnitureController {
    private final FurnitureService furnitureService;

    @PostMapping("/create")
    public ResponseEntity<FurnitureDTO> createFurniture(@RequestBody FurnitureDTO dto) {
        return ResponseEntity.ok(furnitureService.createFurniture(dto));
    }

    @GetMapping("/by-carpenter/{carpenterId}")
    public ResponseEntity<List<FurnitureDTO>> getFurnitureByCarpenterId(@PathVariable Long carpenterId) {
        return ResponseEntity.ok(furnitureService.getFurnituresByCarpenterId(carpenterId));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<FurnitureDTO> updateFurniture(@PathVariable Long id, @RequestBody FurnitureDTO dto) {
        return ResponseEntity.ok(furnitureService.updateFurniture(id, dto));
    }
}

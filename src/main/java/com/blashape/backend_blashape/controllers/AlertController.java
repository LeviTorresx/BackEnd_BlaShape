package com.blashape.backend_blashape.controllers;

import com.blashape.backend_blashape.DTOs.AlertDTO;
import com.blashape.backend_blashape.services.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api_BS/alert")
@RequiredArgsConstructor
public class AlertController {
    private final AlertService alertService;

    @PostMapping("/create")
    public ResponseEntity<AlertDTO> createAlert(@RequestBody AlertDTO dto) {
        return ResponseEntity.ok(alertService.createAlert(dto));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<AlertDTO> getAlert(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlert(id));
    }

    @GetMapping("/by-carpenter/{carpenterId}")
    public ResponseEntity<List<AlertDTO>> getAlertByCarpenterId(@PathVariable Long carpenterId) {
        return ResponseEntity.ok(alertService.getAlertsByCarpenterId(carpenterId));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<AlertDTO> updateAlert(@PathVariable Long id, @RequestBody AlertDTO dto) {
        return ResponseEntity.ok(alertService.updateAlert(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        alertService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }
}

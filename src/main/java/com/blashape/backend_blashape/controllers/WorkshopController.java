package com.blashape.backend_blashape.controllers;

import com.blashape.backend_blashape.DTOs.WorkshopDTO;
import com.blashape.backend_blashape.services.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api_BS/workshop")
@RequiredArgsConstructor
public class WorkshopController {

    private final WorkshopService workshopService;
    private final String mKey = "message";


    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createWorkshop(@RequestBody WorkshopDTO dto) {
        WorkshopDTO newWorkshop = workshopService.createWorkshop(dto);
        return ResponseEntity.ok(Map.of(mKey,"Taller " +newWorkshop.getName() +" creado correctamente"));
    }


    @GetMapping("/get/{id}")
    public ResponseEntity<WorkshopDTO> getWorkshop(@PathVariable Long id) {
        return ResponseEntity.ok(workshopService.getWorkshop(id));
    }

    // Obtener todos los talleres
    /*
    * @GetMapping
    * public ResponseEntity<List<WorkshopDTO>> getAllWorkshops() {
    *     return ResponseEntity.ok(workshopService.getAllWorkshops());
    * }
    */

    @GetMapping("/by-carpenter/{carpenterId}")
    public ResponseEntity<WorkshopDTO> getWorkshopByCarpenterId(@PathVariable Long carpenterId) {
        return ResponseEntity.ok(workshopService.getWorkshopByCarpenterId(carpenterId));
    }


    @PutMapping("/edit/{id}")
    public ResponseEntity<WorkshopDTO> updateWorkshop(@PathVariable Long id, @RequestBody WorkshopDTO dto) {
        return ResponseEntity.ok(workshopService.updateWorkshop(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteWorkshop(@PathVariable Long id) {
        workshopService.deleteWorkshop(id);
        return ResponseEntity.noContent().build();
    }
}


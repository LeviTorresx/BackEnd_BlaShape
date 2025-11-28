package com.blashape.backend_blashape.controllers;

import com.blashape.backend_blashape.DTOs.AlertDTO;
import com.blashape.backend_blashape.DTOs.FurnitureDTO;
import com.blashape.backend_blashape.DTOs.RequestFurniture;
import com.blashape.backend_blashape.services.FurnitureService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api_BS/furniture")
@RequiredArgsConstructor
public class FurnitureController {
    private final FurnitureService furnitureService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FurnitureDTO> createFurniture(
            @RequestParam("data") String data,
            @RequestPart(value = "imageInit")MultipartFile imageInit,
            @RequestPart(value = "imageEnd", required = false) MultipartFile imageEnd,
            @RequestPart (value ="document", required = false) MultipartFile document) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        RequestFurniture requestFurniture = mapper.readValue(data, RequestFurniture.class);

        return ResponseEntity.ok(furnitureService.createFurniture(requestFurniture, imageInit, imageEnd, document));
    }

    @GetMapping("/by-carpenter/{carpenterId}")
    public ResponseEntity<List<FurnitureDTO>> getFurnitureByCarpenterId(@PathVariable Long carpenterId) {
        return ResponseEntity.ok(furnitureService.getFurnituresByCarpenterId(carpenterId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<FurnitureDTO>> getAllByCarpenter(
            @CookieValue(name = "jwt", required = false) String token
    ) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<FurnitureDTO> furniture = furnitureService.getFurnitureByToken(token);
        return ResponseEntity.ok(furniture);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<FurnitureDTO> updateFurniture(@PathVariable Long id, @RequestBody FurnitureDTO dto) {
        return ResponseEntity.ok(furnitureService.updateFurniture(id, dto));
    }
}

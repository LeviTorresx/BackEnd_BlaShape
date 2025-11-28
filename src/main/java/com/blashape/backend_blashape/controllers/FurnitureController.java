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
import java.util.Map;

@RestController
@RequestMapping("/api_BS/furniture")
@RequiredArgsConstructor
public class FurnitureController {
    private final FurnitureService furnitureService;
    private final String mkey = "message";

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public  ResponseEntity<Map<String, String>> createFurniture(
            @RequestParam("data") String data,
            @RequestPart(value = "imageInit")MultipartFile imageInit,
            @RequestPart(value = "imageEnd", required = false) MultipartFile imageEnd,
            @RequestPart (value ="document", required = false) MultipartFile document) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        RequestFurniture requestFurniture = mapper.readValue(data, RequestFurniture.class);
        requestFurniture.setImageInit(imageInit);
        requestFurniture.setImageEnd(imageEnd);
        requestFurniture.setDocument(document);

        FurnitureDTO dto = furnitureService.createFurniture(requestFurniture);

        return ResponseEntity.ok(Map.of(mkey, "Mueble "+dto.getName()+" creado exitosamente"));
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

    @PutMapping(value = "/edit/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> updateFurniture(
            @PathVariable Long id,
            @RequestParam("data") String data,
            @RequestPart(value = "imageInit", required = false) MultipartFile imageInit,
            @RequestPart(value = "imageEnd", required = false) MultipartFile imageEnd,
            @RequestPart (value ="document", required = false) MultipartFile document) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        RequestFurniture requestFurniture = mapper.readValue(data, RequestFurniture.class);
        requestFurniture.setImageInit(imageInit);
        requestFurniture.setImageEnd(imageEnd);
        requestFurniture.setDocument(document);

        FurnitureDTO dto = furnitureService.updateFurniture(id, requestFurniture);

        return ResponseEntity.ok(Map.of(mkey, "Mueble "+dto.getName()+" actualizado exitosamente"));
    }
}

package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.SheetDTO;
import com.blashape.backend_blashape.entitys.Material;
import com.blashape.backend_blashape.entitys.Sheet;
import com.blashape.backend_blashape.repositories.MaterialRepository;
import com.blashape.backend_blashape.repositories.SheetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SheetService {

    private final SheetRepository sheetRepository;
    private final MaterialRepository materialRepository;
    private final ObjectMapper objectMapper;

    public SheetDTO createSheet(SheetDTO dto) {
        if (dto.getHeight() == null || dto.getHeight() <= 0) {
            throw new IllegalArgumentException("La altura de la lámina es obligatoria y debe ser mayor a 0");
        }
        if (dto.getWidth() == null || dto.getWidth() <= 0) {
            throw new IllegalArgumentException("El ancho de la lámina es obligatorio y debe ser mayor a 0");
        }
        if (dto.getMaterialId() == null) {
            throw new IllegalArgumentException("Debe indicar el ID del material para la lámina");
        }

        Material material = materialRepository.findById(dto.getMaterialId())
                .orElseThrow(() -> new EntityNotFoundException("Material no encontrado con ID: " + dto.getMaterialId()));

        Sheet sheet = objectMapper.convertValue(dto, Sheet.class);
        sheet.setMaterial(material);

        Sheet saved = sheetRepository.save(sheet);
        SheetDTO response = objectMapper.convertValue(saved, SheetDTO.class);
        response.setMaterialId(material.getMaterialId());

        return response;
    }

    public SheetDTO getSheet(Long id) {
        Sheet sheet = sheetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lámina no encontrada con ID: " + id));

        SheetDTO dto = objectMapper.convertValue(sheet, SheetDTO.class);
        dto.setMaterialId(sheet.getMaterial().getMaterialId());
        return dto;
    }

    public List<SheetDTO> getAllSheets() {
        return sheetRepository.findAll()
                .stream()
                .map(sheet -> {
                    SheetDTO dto = objectMapper.convertValue(sheet, SheetDTO.class);
                    dto.setMaterialId(sheet.getMaterial().getMaterialId());
                    return dto;
                })
                .toList();
    }

    public SheetDTO updateSheet(Long id, SheetDTO dto) {
        Sheet sheet = sheetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lámina no encontrada con ID: " + id));

        if (dto.getHeight() != null && dto.getHeight() > 0) {
            sheet.setHeight(dto.getHeight());
        }
        if (dto.getWidth() != null && dto.getWidth() > 0) {
            sheet.setWidth(dto.getWidth());
        }
        if (dto.getMaterialId() != null) {
            Material material = materialRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new EntityNotFoundException("Material no encontrado con ID: " + dto.getMaterialId()));
            sheet.setMaterial(material);
        }

        Sheet updated = sheetRepository.save(sheet);
        SheetDTO response = objectMapper.convertValue(updated, SheetDTO.class);
        response.setMaterialId(updated.getMaterial().getMaterialId());

        return response;
    }
    
    public void deleteSheet(Long id) {
        Sheet sheet = sheetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lámina no encontrada con ID: " + id));

        sheetRepository.delete(sheet);
    }
}


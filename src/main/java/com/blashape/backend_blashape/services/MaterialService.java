package com.blashape.backend_blashape.services;


import com.blashape.backend_blashape.DTOs.MaterialDTO;
import com.blashape.backend_blashape.entitys.Material;
import com.blashape.backend_blashape.repositories.MaterialRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final ObjectMapper objectMapper;

    public MaterialDTO createMaterial(MaterialDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del material es obligatorio");
        }
        if (dto.getThickness() == null || dto.getThickness() <= 0){
            throw new IllegalArgumentException("El grosor del material es obligatorio y mayor a 0");
        }
        if (dto.getColorName() == null || dto.getColorName().isBlank()){
            throw new IllegalArgumentException("El nombre del color es obligatorio");
        }
        if (dto.getColorHex() == null || dto.getColorHex().isBlank()){
            throw new IllegalArgumentException("El código del color es obligatorio");
        }

        Material material = objectMapper.convertValue(dto, Material.class);
        Material saved = materialRepository.save(material);

        return objectMapper.convertValue(saved, MaterialDTO.class);
    }

    public MaterialDTO getMaterial(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Material no encontrado con ID: " + id));

        return objectMapper.convertValue(material, MaterialDTO.class);
    }

    public List<MaterialDTO> getAllMaterials() {
        return materialRepository.findAll()
                .stream()
                .map(material -> objectMapper.convertValue(material, MaterialDTO.class))
                .toList();
    }

    public MaterialDTO updateMaterial(Long id, MaterialDTO dto) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Material no encontrado con ID: " + id));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            material.setName(dto.getName());
        }
        if (dto.getThickness() != null && dto.getThickness() > 0) {
            material.setThickness(dto.getThickness());
        }
        if (dto.getColorName() != null && !dto.getColorName().isBlank()) {
            material.setColorName(dto.getColorName());
        }
        if (dto.getColorHex() != null && !dto.getColorHex().isBlank()) {
            material.setColorHex(dto.getColorHex());
        }

        Material updated = materialRepository.save(material);
        return objectMapper.convertValue(updated, MaterialDTO.class);
    }

    public void deleteMaterial(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Material no encontrado con ID: " + id));

        materialRepository.delete(material);
    }

}

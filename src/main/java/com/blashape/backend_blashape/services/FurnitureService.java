package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.AlertDTO;
import com.blashape.backend_blashape.DTOs.FurnitureDTO;
import com.blashape.backend_blashape.entitys.Alert;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.entitys.Furniture;
import com.blashape.backend_blashape.mapper.FurnitureMapper;
import com.blashape.backend_blashape.repositories.FurnitureRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FurnitureService {
    private final FurnitureRepository furnitureRepository;
    private final FurnitureMapper furnitureMapper;
    private final CarpenterRepository carpenterRepository;

    public FurnitureDTO createFurniture(FurnitureDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del mueble es obligatorio");
        }
        if (dto.getImageInitUrl() == null || dto.getImageInitUrl().isBlank()) {
            throw new IllegalArgumentException("La imagen inicial del mueble es obligatoria");
        }
        if (dto.getCreationDate() == null || dto.getEndDate() == null) {
            throw new IllegalArgumentException("Las fechas son obligatorias");
        }
        if (dto.getStatus() == null) {
            throw new IllegalArgumentException("El estado del mueble es obligatorio");
        }
        if (dto.getCarpenterId() == null) {
            throw new IllegalArgumentException("Debe indicar el ID del carpintero que crea el mueble");
        }

        Furniture furniture = furnitureMapper.toEntity(dto);

        if (dto.getCarpenterId() != null) {
            Carpenter carpenter = carpenterRepository.findById(dto.getCarpenterId())
                    .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado"));
            furniture.setCarpenter(carpenter);
        }

        Furniture saved = furnitureRepository.save(furniture);
        return furnitureMapper.toDTO(saved);
    }

    public List<FurnitureDTO> getFurnituresByCarpenterId(Long carpenterId) {
        Carpenter carpenter = carpenterRepository.findById(carpenterId)
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado con ID: " + carpenterId));

        List<Furniture> furnitures = furnitureRepository.findByCarpenter(carpenter);

        return furnitures.stream()
                .map(furnitureMapper::toDTO)
                .toList();
    }

    public FurnitureDTO updateFurniture(Long Id, FurnitureDTO dto) {
        Furniture furniture = furnitureRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("Mueble no encontrado con ID: " + Id));

        if(dto.getName() == null || dto.getName().isBlank()){
            throw new IllegalArgumentException("El mueble debe tener nombre");
        }

        furniture.setName(dto.getName());

        if (dto.getImageInitUrl() != null) {
            furniture.setImageInitURL(dto.getImageInitUrl());
        }
        if (dto.getImageEndUrl() != null) {
            furniture.setImageEndURL(dto.getImageEndUrl());
        }
        if (dto.getDocumentUrl() != null) {
            furniture.setDocumentURL(dto.getDocumentUrl());
        }
        if (dto.getEndDate() != null) {
            furniture.setEndDate(dto.getEndDate());
        }
        if (dto.getStatus() != null) {
            furniture.setStatus(dto.getStatus());
        }

        Furniture updated =  furnitureRepository.save(furniture);
        return furnitureMapper.toDTO(updated);
    }
}

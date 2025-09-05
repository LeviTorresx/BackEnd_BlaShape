package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.PieceDTO;
import com.blashape.backend_blashape.entitys.Edges;
import com.blashape.backend_blashape.entitys.Furniture;
import com.blashape.backend_blashape.entitys.Material;
import com.blashape.backend_blashape.entitys.Piece;
import com.blashape.backend_blashape.repositories.FurnitureRepository;
import com.blashape.backend_blashape.repositories.MaterialRepository;
import com.blashape.backend_blashape.repositories.PieceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PieceService {

    private final PieceRepository pieceRepository;
    private final FurnitureRepository furnitureRepository;
    private final MaterialRepository     materialRepository;
    private final ObjectMapper objectMapper;

    public PieceDTO createPiece(PieceDTO dto) {
        if (dto.getHeight() == null || dto.getHeight() <= 0) {
            throw new IllegalArgumentException("La altura de la pieza es obligatoria y debe ser mayor a 0");
        }
        if (dto.getWidth() == null || dto.getWidth() <= 0) {
            throw new IllegalArgumentException("El ancho de la pieza es obligatorio y debe ser mayor a 0");
        }
        if (dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("La cantidad de piezas debe ser mayor a 0");
        }
        if (dto.getMaterialId() == null) {
            throw new IllegalArgumentException("Debe indicar el ID del material de la pieza");
        }
        if (dto.getFurnitureId() == null) {
            throw new IllegalArgumentException("Debe indicar el ID del mueble al que pertenece la pieza");
        }

        Material material = materialRepository.findById(dto.getMaterialId())
                .orElseThrow(() -> new EntityNotFoundException("Material no encontrado con ID: " + dto.getMaterialId()));

        Furniture furniture = furnitureRepository.findById(dto.getFurnitureId())
                .orElseThrow(() -> new EntityNotFoundException("Mueble no encontrado con ID: " + dto.getFurnitureId()));

        Piece piece = objectMapper.convertValue(dto, Piece.class);
        piece.setMaterial(material);
        piece.setFurniture(furniture);

        Piece saved = pieceRepository.save(piece);

        PieceDTO response = objectMapper.convertValue(saved, PieceDTO.class);
        response.setMaterialId(material.getMaterialId());
        response.setFurnitureId(furniture.getFurnitureId());
        return response;
    }

    public PieceDTO getPiece(Long id) {
        Piece piece = pieceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pieza no encontrada con ID: " + id));

        PieceDTO dto = objectMapper.convertValue(piece, PieceDTO.class);
        dto.setMaterialId(piece.getMaterial().getMaterialId());
        dto.setFurnitureId(piece.getFurniture().getFurnitureId());
        return dto;
    }


    public List<PieceDTO> getAllPieces() {
        return pieceRepository.findAll()
                .stream()
                .map(piece -> {
                    PieceDTO dto = objectMapper.convertValue(piece, PieceDTO.class);
                    dto.setMaterialId(piece.getMaterial().getMaterialId());
                    dto.setFurnitureId(piece.getFurniture().getFurnitureId());
                    return dto;
                })
                .toList();
    }

    public PieceDTO updatePiece(Long id, PieceDTO dto) {
        Piece piece = pieceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pieza no encontrada con ID: " + id));

        if (dto.getHeight() != null && dto.getHeight() > 0) {
            piece.setHeight(dto.getHeight());
        }
        if (dto.getWidth() != null && dto.getWidth() > 0) {
            piece.setWidth(dto.getWidth());
        }
        if (dto.getQuantity() > 0) {
            piece.setQuantity(dto.getQuantity());
        }
        if (dto.getMaterialId() != null) {
            Material material = materialRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new EntityNotFoundException("Material no encontrado con ID: " + dto.getMaterialId()));
            piece.setMaterial(material);
        }
        if (dto.getFurnitureId() != null) {
            Furniture furniture = furnitureRepository.findById(dto.getFurnitureId())
                    .orElseThrow(() -> new EntityNotFoundException("Mueble no encontrado con ID: " + dto.getFurnitureId()));
            piece.setFurniture(furniture);
        }
        if (dto.getEdges() != null) {
            piece.setEdges(objectMapper.convertValue(dto.getEdges(), Edges.class));
        }

        Piece updated = pieceRepository.save(piece);

        PieceDTO response = objectMapper.convertValue(updated, PieceDTO.class);
        response.setMaterialId(updated.getMaterial().getMaterialId());
        response.setFurnitureId(updated.getFurniture().getFurnitureId());
        return response;
    }


    public void deletePiece(Long id) {
        Piece piece = pieceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pieza no encontrada con ID: " + id));

        pieceRepository.delete(piece);
    }
}


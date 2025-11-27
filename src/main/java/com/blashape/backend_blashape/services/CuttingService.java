package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.CuttingDTO;
import com.blashape.backend_blashape.DTOs.PieceDTO;
import com.blashape.backend_blashape.entitys.Cutting;
import com.blashape.backend_blashape.entitys.Furniture;
import com.blashape.backend_blashape.entitys.Piece;
import com.blashape.backend_blashape.mapper.CuttingMapper;
import com.blashape.backend_blashape.mapper.PieceMapper;
import com.blashape.backend_blashape.repositories.CuttingRepository;
import com.blashape.backend_blashape.repositories.FurnitureRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CuttingService {

    private final CuttingRepository cuttingRepository;
    private final FurnitureRepository furnitureRepository;
    private final CuttingMapper cuttingMapper;
    private final PieceMapper pieceMapper;

    @Transactional
    public CuttingDTO createCutting(CuttingDTO dto) {

        if (cuttingRepository.existsByFurnitureFurnitureId(dto.getFurnitureId())) {
            throw new RuntimeException("Este mueble ya tiene Cutting, use updateCutting en su lugar");
        }

        Furniture furniture = furnitureRepository.findById(dto.getFurnitureId())
                .orElseThrow(() -> new RuntimeException("Furniture no encontrado"));

        Cutting cutting = cuttingMapper.toEntity(dto);
        cutting.setFurniture(furniture);

        List<Piece> pieces = dto.getPieces().stream()
                .map(pieceMapper::toEntity)
                .peek(p -> p.setCutting(cutting))
                .toList();

        cutting.setPieces(pieces);

        Cutting saved = cuttingRepository.save(cutting);
        return cuttingMapper.toDTO(saved);
    }


    public CuttingDTO getByFurnitureId(Long furnitureId) {

        Cutting cutting = cuttingRepository.findByFurnitureFurnitureId(furnitureId)
                .orElseThrow(() -> new RuntimeException("Este mueble no tiene Cutting asociado"));

        CuttingDTO dto = cuttingMapper.toDTO(cutting);

        return dto;
    }

    @Transactional
    public CuttingDTO updateCutting(Long cuttingId, CuttingDTO dto) {

        Cutting cutting = cuttingRepository.findById(cuttingId)
                .orElseThrow(() -> new RuntimeException("Cutting no encontrado"));

        cutting.setMaterialName(dto.getMaterialName());
        cutting.setSheetQuantity(dto.getSheetQuantity());

        cutting.getPieces().clear();

        if (dto.getPieces() != null) {
            List<Piece> updatedPieces = dto.getPieces().stream()
                    .map(pieceMapper::toEntity)
                    .peek(p -> p.setCutting(cutting))
                    .toList();

            cutting.getPieces().addAll(updatedPieces);
        }

        Cutting saved = cuttingRepository.save(cutting);
        return cuttingMapper.toDTO(saved);
    }
}

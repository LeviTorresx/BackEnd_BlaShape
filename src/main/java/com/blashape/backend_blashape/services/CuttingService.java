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
    public CuttingDTO saveCutting(CuttingDTO dto) {
        Furniture furniture = furnitureRepository.findById(dto.getFurnitureId())
                .orElseThrow(() -> new RuntimeException("Furniture no encontrado con ID: " + dto.getFurnitureId()));

        Cutting cutting = cuttingMapper.toEntity(dto);
        cutting.setFurniture(furniture);

        if (dto.getPieces() != null) {
            List<Piece> pieces = dto.getPieces().stream()
                    .map(pieceMapper::toEntity)
                    .peek(p -> p.setCutting(cutting)) // asignar relación bidireccional
                    .toList();

            cutting.setPieces(pieces);
        }

        Cutting saved = cuttingRepository.save(cutting);

        CuttingDTO response = cuttingMapper.toDTO(saved);

        response.setMaterialName(dto.getMaterialName());
        response.setSheetQuantity(dto.getSheetQuantity());

        return response;
    }

    public CuttingDTO getByFurnitureId(Long furnitureId) {

        Cutting cutting = cuttingRepository.findByFurnitureFurnitureId(furnitureId)
                .orElseThrow(() -> new RuntimeException("Este mueble no tiene Cutting asociado"));

        CuttingDTO dto = cuttingMapper.toDTO(cutting);

        return dto;
    }


}

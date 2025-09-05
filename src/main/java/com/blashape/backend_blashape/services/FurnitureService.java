package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.FurnitureDTO;
import com.blashape.backend_blashape.DTOs.PieceDTO;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.entitys.Customer;
import com.blashape.backend_blashape.entitys.Furniture;
import com.blashape.backend_blashape.entitys.Piece;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.repositories.CustomerRepository;
import com.blashape.backend_blashape.repositories.FurnitureRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FurnitureService {

    private final FurnitureRepository furnitureRepository;
    private final CarpenterRepository carpenterRepository;
    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;

    public FurnitureDTO createFurniture(FurnitureDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del mueble es obligatorio");
        }
        if (dto.getCarpenterId() == null) {
            throw new IllegalArgumentException("Debe indicar el ID del carpintero");
        }
        if (dto.getCustomerId() == null) {
            throw new IllegalArgumentException("Debe indicar el ID del cliente");
        }

        Carpenter carpenter = carpenterRepository.findById(dto.getCarpenterId())
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado con ID: " + dto.getCarpenterId()));

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + dto.getCustomerId()));

        Furniture furniture = objectMapper.convertValue(dto, Furniture.class);
        furniture.setCarpenter(carpenter);
        furniture.setCustomer(customer);

        // ⚡ Manejo de piezas nuevas
        if (dto.getPieces() != null && !dto.getPieces().isEmpty()) {
            List<Piece> pieces = dto.getPieces().stream()
                    .map(pieceDTO -> {
                        Piece piece = objectMapper.convertValue(pieceDTO, Piece.class);
                        piece.setSheetId(null);
                        piece.setFurniture(furniture); // asociación al mueble
                        return piece;
                    })
                    .toList();
            furniture.setPieces(pieces);
        }

        Furniture saved = furnitureRepository.save(furniture);

        FurnitureDTO response = objectMapper.convertValue(saved, FurnitureDTO.class);
        response.setCarpenterId(carpenter.getCarpenterId());
        response.setCustomerId(customer.getCustomerId());

        // ⚡ Convertimos también las piezas creadas
        if (saved.getPieces() != null) {
            List<PieceDTO> pieceDTOs = saved.getPieces().stream()
                    .map(piece -> {
                        PieceDTO pieceDTO = objectMapper.convertValue(piece, PieceDTO.class);
                        pieceDTO.setFurnitureId(saved.getFurnitureId());
                        return pieceDTO;
                    })
                    .toList();
            response.setPieces(pieceDTOs);
        }

        return response;
    }


    public FurnitureDTO getFurniture(Long id) {
        Furniture furniture = furnitureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mueble no encontrado con ID: " + id));

        FurnitureDTO dto = objectMapper.convertValue(furniture, FurnitureDTO.class);
        dto.setCarpenterId(furniture.getCarpenter().getCarpenterId());
        dto.setCustomerId(furniture.getCustomer().getCustomerId());
        return dto;
    }

    public List<FurnitureDTO> getAllFurnitures() {
        return furnitureRepository.findAll()
                .stream()
                .map(furniture -> {
                    FurnitureDTO dto = objectMapper.convertValue(furniture, FurnitureDTO.class);
                    dto.setCarpenterId(furniture.getCarpenter().getCarpenterId());
                    dto.setCustomerId(furniture.getCustomer().getCustomerId());
                    return dto;
                })
                .toList();
    }

    public FurnitureDTO updateFurniture(Long id, FurnitureDTO dto) {
        Furniture furniture = furnitureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mueble no encontrado con ID: " + id));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            furniture.setName(dto.getName());
        }
        if (dto.getDocumentUrl() != null && !dto.getDocumentUrl().isBlank()) {
            furniture.setDocumentUrl(dto.getDocumentUrl());
        }
        if (dto.getImageInitUrl() != null && !dto.getImageInitUrl().isBlank()) {
            furniture.setImageInitUrl(dto.getImageInitUrl());
        }
        if (dto.getImageEndUrl() != null && !dto.getImageEndUrl().isBlank()) {
            furniture.setImageEndUrl(dto.getImageEndUrl());
        }

        if (dto.getCarpenterId() != null) {
            Carpenter carpenter = carpenterRepository.findById(dto.getCarpenterId())
                    .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado con ID: " + dto.getCarpenterId()));
            furniture.setCarpenter(carpenter);
        }

        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + dto.getCustomerId()));
            furniture.setCustomer(customer);
        }

        Furniture updated = furnitureRepository.save(furniture);
        FurnitureDTO response = objectMapper.convertValue(updated, FurnitureDTO.class);
        response.setCarpenterId(updated.getCarpenter().getCarpenterId());
        response.setCustomerId(updated.getCustomer().getCustomerId());
        return response;
    }

    public void deleteFurniture(Long id) {
        Furniture furniture = furnitureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mueble no encontrado con ID: " + id));

        furnitureRepository.delete(furniture);
    }
}


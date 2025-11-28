package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.AlertDTO;
import com.blashape.backend_blashape.DTOs.FurnitureDTO;
import com.blashape.backend_blashape.DTOs.PieceDTO;
import com.blashape.backend_blashape.DTOs.RequestFurniture;
import com.blashape.backend_blashape.config.JwtUtil;
import com.blashape.backend_blashape.entitys.*;
import com.blashape.backend_blashape.mapper.PieceMapper;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.mapper.FurnitureMapper;
import com.blashape.backend_blashape.repositories.CustomerRepository;
import com.blashape.backend_blashape.repositories.FurnitureRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FurnitureService {

    private final FurnitureRepository furnitureRepository;
    private final FurnitureMapper furnitureMapper;
    private final CarpenterRepository carpenterRepository;
    private final JwtUtil jwtUtil;
    private final CustomerRepository customerRepository;
    private final PieceMapper pieceMapper;
    private final CloudinaryService cloudinaryService;
    private static final String FURNITURE_IMAGE_DIR = "furniture/images";
    private static final String FURNITURE_DOCUMENT_DIR = "furniture/docs";

    public FurnitureDTO createFurniture(RequestFurniture requestFurniture,
                                        MultipartFile imageInit,
                                        MultipartFile imageEnd,
                                        MultipartFile document) {

        // Validaciones
        if (requestFurniture.getName() == null || requestFurniture.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del mueble es obligatorio");
        }
        if (imageInit == null || imageInit.isEmpty()) {
            throw new IllegalArgumentException("La imagen inicial del mueble es obligatoria");
        }
        if (requestFurniture.getCreationDate() == null) {
            throw new IllegalArgumentException("La fecha de creación es obligatoria");
        }
        if (requestFurniture.getStatus() == null) {
            throw new IllegalArgumentException("El estado del mueble es obligatorio");
        }
        if (requestFurniture.getType() == null) {
            throw new IllegalArgumentException("Debe indicar el tipo de mueble");
        }
        if (requestFurniture.getCarpenterId() == null) {
            throw new IllegalArgumentException("Debe indicar el ID del carpintero que crea el mueble");
        }

        try{
            if(imageInit != null && !imageInit.isEmpty()){
                Map uploadResult = cloudinaryService.uploadFile(imageInit);

            }
        }catch(Exception e){}

        FurnitureDTO dto = furnitureMapper.toDto(requestFurniture);

        // Mapear DTO → Entity
        Furniture furniture = furnitureMapper.toEntity(dto);

        // Asignar carpenter
        Carpenter carpenter = carpenterRepository.findById(dto.getCarpenterId())
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado"));
        furniture.setCarpenter(carpenter);

        // Asignar customer (opcional)
        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
            furniture.setCustomer(customer);
        }

        // Cutting
        if (furniture.getCutting() == null) {
            Cutting emptyCutting = new Cutting();
            emptyCutting.setFurniture(furniture);
            emptyCutting.setPieces(new ArrayList<>());
            furniture.setCutting(emptyCutting);
        } else {
            furniture.getCutting().setFurniture(furniture);
            if (furniture.getCutting().getPieces() == null) {
                furniture.getCutting().setPieces(new ArrayList<>());
            }
        }

        // Guardar
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

    public List<FurnitureDTO> getFurnitureByToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token no proporcionado");
        }

        String email = jwtUtil.extractEmail(token);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Token inválido o expirado");
        }

        Carpenter carpenter = carpenterRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado para el token"));

        List<Furniture> furniture = furnitureRepository.findFurnitureByCarpenterId(
                carpenter.getCarpenterId()
        );

        return furniture.stream()
                .map(furnitureMapper::toDTO)
                .toList();
    }


    @Transactional
    public FurnitureDTO updateFurniture(Long id, FurnitureDTO dto) {

        Furniture furniture = furnitureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mueble no encontrado con ID: " + id));

        // --- PROPIEDADES BÁSICAS ---
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("El mueble debe tener nombre");
        }

        furniture.setName(dto.getName());
        if (dto.getImageInitUrl() != null) furniture.setImageInitURL(dto.getImageInitUrl());
        if (dto.getImageEndUrl() != null) furniture.setImageEndURL(dto.getImageEndUrl());
        if (dto.getDocumentUrl() != null) furniture.setDocumentURL(dto.getDocumentUrl());
        if (dto.getEndDate() != null) furniture.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) furniture.setStatus(dto.getStatus());
        if (dto.getType() != null) furniture.setType(dto.getType());

        // --- ACTUALIZAR CARPENTER ---
        if (dto.getCarpenterId() != null) {
            Carpenter carpenter = carpenterRepository.findById(dto.getCarpenterId())
                    .orElseThrow(() -> new RuntimeException("Carpenter no encontrado"));
            furniture.setCarpenter(carpenter);
        }

        // --- ACTUALIZAR CUSTOMER ---
        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer no encontrado"));
            furniture.setCustomer(customer);
        }

        // --- ACTUALIZAR / CREAR CUTTING ---
        Cutting cutting = furniture.getCutting();

        if (dto.getCutting() != null) {

            // SI NO EXISTE, CREARLO
            if (cutting == null) {
                cutting = new Cutting();
                cutting.setFurniture(furniture);
                furniture.setCutting(cutting);
            }

            // ACTUALIZAR CAMPOS DEL CUTTING
            cutting.setMaterialName(dto.getCutting().getMaterialName());
            cutting.setSheetQuantity(dto.getCutting().getSheetQuantity());

            // --- ACTUALIZAR PIECES ---
            List<PieceDTO> dtoPieces = dto.getCutting().getPieces();

            // Si vienen null → lista vacía
            if (dtoPieces == null) dtoPieces = List.of();

            // Limpiar piezas actuales
            cutting.getPieces().clear();

            // Reconstruir piezas
            for (PieceDTO pDto : dtoPieces) {
                Piece piece = pieceMapper.toEntity(pDto);
                piece.setCutting(cutting); // MUY IMPORTANTE
                cutting.getPieces().add(piece);
            }
        }

        Furniture saved = furnitureRepository.save(furniture);

        return furnitureMapper.toDTO(saved);
    }

}


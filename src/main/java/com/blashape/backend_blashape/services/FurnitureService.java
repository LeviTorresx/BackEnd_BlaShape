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

    public FurnitureDTO createFurniture(RequestFurniture request) {

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del mueble es obligatorio");
        }
        if (request.getCreationDate() == null) {
            throw new IllegalArgumentException("La fecha de creación es obligatoria");
        }
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("El estado del mueble es obligatorio");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("Debe indicar el tipo de mueble");
        }
        if (request.getCarpenterId() == null) {
            throw new IllegalArgumentException("Debe indicar el ID del carpintero");
        }

        String imageInitUrl = uploadIfPresent(request.getImageInit(), "imagen inicial");
        String imageEndUrl = uploadIfPresent(request.getImageEnd(), "imagen final");
        String documentUrl = uploadIfPresent(request.getDocument(), "documento");

        // ==== MAPEO Request → DTO ====
        FurnitureDTO dto = furnitureMapper.toDTO(request);
        dto.setImageInitURL(imageInitUrl);
        dto.setImageEndURL(imageEndUrl);
        dto.setDocumentURL(documentUrl);

        // ==== MAPEO DTO → ENTITY ====
        Furniture furniture = furnitureMapper.toEntity(dto);

        // Carpintero obligatorio
        Carpenter carpenter = carpenterRepository.findById(dto.getCarpenterId())
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado"));
        furniture.setCarpenter(carpenter);

        // Cliente opcional
        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
            furniture.setCustomer(customer);
        }

        // ==== CUTTING ====
        if (furniture.getCutting() == null) {
            Cutting c = new Cutting();
            c.setFurniture(furniture);
            c.setPieces(new ArrayList<>());
            furniture.setCutting(c);
        } else {
            furniture.getCutting().setFurniture(furniture);
            if (furniture.getCutting().getPieces() == null) {
                furniture.getCutting().setPieces(new ArrayList<>());
            }
        }

        // ==== GUARDAR ====
        Furniture saved = furnitureRepository.save(furniture);

        return furnitureMapper.toDTO(saved);
    }

    /**
     * Sube archivo opcionalmente; si está vacío, devuelve string vacío.
     * Si falla lanza excepción con mensaje claro.
     */
    private String uploadIfPresent(MultipartFile file, String nombreCampo) {
        if (file == null || file.isEmpty()) return "";

        try {
            Map result = cloudinaryService.uploadFile(file);
            return result.get("secure_url").toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al subir " + nombreCampo + ": " + e.getMessage());
        }
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
        if (dto.getImageInitURL() != null) furniture.setImageInitURL(dto.getImageInitURL());
        if (dto.getImageEndURL() != null) furniture.setImageEndURL(dto.getImageEndURL());
        if (dto.getDocumentURL() != null) furniture.setDocumentURL(dto.getDocumentURL());
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


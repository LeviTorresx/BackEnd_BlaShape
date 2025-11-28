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

import java.time.LocalDate;
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

        String imageInitUrl = uploadImageIfPresent(request.getImageInit());
        String imageEndUrl = uploadImageIfPresent(request.getImageEnd());
        String documentUrl = uploadDocumentIfPresent(request.getDocument());

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

    private String uploadImageIfPresent(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            return cloudinaryService.uploadImage(file);
        } catch (Exception e) {
            throw new RuntimeException("Error al subir imagen: " + e.getMessage());
        }
    }

    private String uploadDocumentIfPresent(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            return cloudinaryService.uploadDocument(file);
        } catch (Exception e) {
            throw new RuntimeException("Error al subir documento: " + e.getMessage());
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
    public FurnitureDTO updateFurniture(Long id, RequestFurniture request) {

        // ====== BUSCAR EL MUEBLE ======
        Furniture furniture = furnitureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mueble no encontrado con ID: " + id));

        // ====== VALIDACIONES ======
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("El mueble debe tener nombre");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("El tipo de mueble es obligatorio");
        }

        // ====== SUBIDA OPCIONAL DE ARCHIVOS ======
        String newImageInit = uploadImageIfPresent(request.getImageInit());
        String newImageEnd  = uploadImageIfPresent(request.getImageEnd());
        String newDocument  = uploadDocumentIfPresent(request.getDocument());

        // ====== ACTUALIZAR CAMPOS SIMPLES ======
        furniture.setName(request.getName());
        furniture.setStatus(request.getStatus());
        furniture.setType(request.getType());

        if (request.getEndDate() != null && !request.getEndDate().isBlank()) {
            furniture.setEndDate(LocalDate.parse(request.getEndDate()));
        }

        if (request.getCreationDate() != null && !request.getCreationDate().isBlank()) {
            furniture.setCreationDate(LocalDate.parse(request.getCreationDate()));
        }

        // Solo actualizar si se subió archivo nuevo
        if (!newImageInit.isEmpty()) furniture.setImageInitURL(newImageInit);
        if (!newImageEnd.isEmpty())  furniture.setImageEndURL(newImageEnd);
        if (!newDocument.isEmpty())  furniture.setDocumentURL(newDocument);

        // ====== ACTUALIZAR CARPENTER ======
        if (request.getCarpenterId() != null) {
            Carpenter carpenter = carpenterRepository.findById(request.getCarpenterId())
                    .orElseThrow(() -> new RuntimeException("Carpintero no encontrado"));
            furniture.setCarpenter(carpenter);
        }

        // ====== ACTUALIZAR CUSTOMER ======
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            furniture.setCustomer(customer);
        }

        // ====== ACTUALIZAR CUTTING ======
        if (request.getCutting() != null) {

            Cutting cutting = furniture.getCutting();

            if (cutting == null) {
                cutting = new Cutting();
                cutting.setFurniture(furniture);
                furniture.setCutting(cutting);
            }

            cutting.setMaterialName(request.getCutting().getMaterialName());
            cutting.setSheetQuantity(request.getCutting().getSheetQuantity());

            // Limpiar piezas actuales
            cutting.getPieces().clear();

            if (request.getCutting().getPieces() != null) {
                for (PieceDTO pieceDTO : request.getCutting().getPieces()) {
                    Piece piece = pieceMapper.toEntity(pieceDTO);
                    piece.setCutting(cutting);
                    cutting.getPieces().add(piece);
                }
            }
        }

        // ====== GUARDAR ======
        Furniture saved = furnitureRepository.save(furniture);

        // Mapstruct devuelve DTO listo para respuesta
        return furnitureMapper.toDTO(saved);
    }

}


package com.blashape.backend_blashape.services;

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
import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional
    public FurnitureDTO createFurniture(RequestFurniture request) {

        validateRequest(request);

        String imageInitUrl = uploadImageIfPresent(request.getImageInit());
        String imageEndUrl  = uploadImageIfPresent(request.getImageEnd());
        String documentUrl  = uploadDocumentIfPresent(request.getDocument());

        Furniture furniture = furnitureMapper.toEntity(request);

        furniture.setImageInitURL(imageInitUrl);
        furniture.setImageEndURL(imageEndUrl);
        furniture.setDocumentURL(documentUrl);

        Carpenter carpenter = carpenterRepository.findById(request.getCarpenterId())
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado"));
        furniture.setCarpenter(carpenter);

        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
            furniture.setCustomer(customer);
        }

        // ===== CUTTING =====
        Cutting cutting = new Cutting();
        cutting.setFurniture(furniture);

        if (request.getCutting() != null) {
            cutting.setMaterialName(request.getCutting().getMaterialName());
            cutting.setSheetQuantity(request.getCutting().getSheetQuantity());
        }

        List<Piece> pieces = buildPiecesFromDTO(request, cutting);
        cutting.setPieces(pieces);

        furniture.setCutting(cutting);

        Furniture saved = furnitureRepository.save(furniture);

        return furnitureMapper.toDTO(saved);
    }

    @Transactional
    public FurnitureDTO updateFurniture(Long id, RequestFurniture request) {

        Furniture furniture = furnitureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mueble no encontrado con ID: " + id));

        validateUpdate(request);
        optionalFileUploadForUpdate(furniture, request);

        // ===== BASIC FIELDS =====
        furniture.setName(request.getName());
        furniture.setStatus(request.getStatus());
        furniture.setType(request.getType());

        if (request.getCreationDate() != null && !request.getCreationDate().isBlank()) {
            furniture.setCreationDate(LocalDate.parse(request.getCreationDate()));
        }

        if (request.getEndDate() != null && !request.getEndDate().isBlank()) {
            furniture.setEndDate(LocalDate.parse(request.getEndDate()));
        }

        // ===== RELATIONS =====
        if (request.getCarpenterId() != null) {
            Carpenter carpenter = carpenterRepository.findById(request.getCarpenterId())
                    .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado"));
            furniture.setCarpenter(carpenter);
        }

        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
            furniture.setCustomer(customer);
        }

        // ===== CUTTING =====
        if (request.getCutting() != null) {
            Cutting cutting = furniture.getCutting();
            if (cutting == null) {
                cutting = new Cutting();
                cutting.setFurniture(furniture);
                cutting.setPieces(new ArrayList<>());
                furniture.setCutting(cutting);
            }

            cutting.setMaterialName(request.getCutting().getMaterialName());
            cutting.setSheetQuantity(request.getCutting().getSheetQuantity());

            List<Piece> existingPieces = cutting.getPieces();
            if (existingPieces == null) {
                existingPieces = new ArrayList<>();
                cutting.setPieces(existingPieces);
            }

            Map<Long, Piece> existingMap = existingPieces.stream()
                    .filter(p -> p.getPieceId() != null)
                    .collect(Collectors.toMap(Piece::getPieceId, p -> p));

            Set<Long> incomingIds = new HashSet<>();

            if (request.getCutting().getPieces() != null) {
                for (PieceDTO dto : request.getCutting().getPieces()) {
                    if (dto.getPieceId() != null && existingMap.containsKey(dto.getPieceId())) {
                        // ✅ Actualiza en sitio — nunca eliminar y volver a agregar
                        Piece entity = existingMap.get(dto.getPieceId());
                        pieceMapper.updateEntityFromDTO(dto, entity);
                        incomingIds.add(dto.getPieceId());
                    } else {
                        // ✅ Pieza nueva
                        Piece newPiece = pieceMapper.toEntity(dto);
                        newPiece.setCutting(cutting);
                        existingPieces.add(newPiece);
                    }
                }
            }

            // ✅ Elimina solo las piezas que no vienen en el request
            existingPieces.removeIf(p -> p.getPieceId() != null && !incomingIds.contains(p.getPieceId()));
        }

        Furniture saved = furnitureRepository.save(furniture);
        return furnitureMapper.toDTO(saved);
    }

    private List<Piece> buildPiecesFromDTO(RequestFurniture request, Cutting cutting) {

        List<Piece> pieces = new ArrayList<>();

        if (request.getCutting() != null && request.getCutting().getPieces() != null) {
            for (PieceDTO dto : request.getCutting().getPieces()) {

                Piece piece = pieceMapper.toEntity(dto);

                piece.setPieceId(null); // evita conflictos
                piece.setCutting(cutting); // 🔥 RELACIÓN OBLIGATORIA

                pieces.add(piece);
            }
        }

        return pieces;
    }

    private void validateRequest(RequestFurniture request) {
        if (request.getName() == null || request.getName().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio");

        if (request.getCreationDate() == null)
            throw new IllegalArgumentException("La fecha es obligatoria");

        if (request.getStatus() == null)
            throw new IllegalArgumentException("El estado es obligatorio");

        if (request.getType() == null)
            throw new IllegalArgumentException("El tipo es obligatorio");

        if (request.getCarpenterId() == null)
            throw new IllegalArgumentException("El carpintero es obligatorio");
    }

    private void validateUpdate(RequestFurniture request) {
        if (request.getName() == null || request.getName().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio");

        if (request.getType() == null)
            throw new IllegalArgumentException("El tipo es obligatorio");
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

    private void optionalFileUploadForUpdate(Furniture furniture, RequestFurniture request) {
        String newImageInit = uploadImageIfPresent(request.getImageInit());
        String newImageEnd  = uploadImageIfPresent(request.getImageEnd());
        String newDocument  = uploadDocumentIfPresent(request.getDocument());

        if (newImageInit != null) furniture.setImageInitURL(newImageInit);
        if (newImageEnd  != null) furniture.setImageEndURL(newImageEnd);
        if (newDocument  != null) furniture.setDocumentURL(newDocument);
    }

    public List<FurnitureDTO> getFurnituresByCarpenterId(Long carpenterId) {

        Carpenter carpenter = carpenterRepository.findById(carpenterId)
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado"));

        return furnitureRepository.findByCarpenter(carpenter)
                .stream()
                .map(furnitureMapper::toDTO)
                .toList();
    }

    public List<FurnitureDTO> getFurnitureByToken(String token) {

        String email = jwtUtil.extractEmail(token);

        Carpenter carpenter = carpenterRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado"));

        return furnitureRepository.findFurnitureByCarpenterId(carpenter.getCarpenterId())
                .stream()
                .map(furnitureMapper::toDTO)
                .toList();
    }
}
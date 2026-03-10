package com.blashape.backend_blashape;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.blashape.backend_blashape.DTOs.*;
import com.blashape.backend_blashape.config.JwtUtil;
import com.blashape.backend_blashape.entitys.*;
import com.blashape.backend_blashape.mapper.*;
import com.blashape.backend_blashape.repositories.*;
import com.blashape.backend_blashape.services.*;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class FurnitureServiceTest {

    @Mock
    private FurnitureRepository furnitureRepository;

    @Mock
    private FurnitureMapper furnitureMapper;

    @Mock
    private CarpenterRepository carpenterRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PieceMapper pieceMapper;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private FurnitureService furnitureService;

    private RequestFurniture request;
    private Furniture furniture;
    private FurnitureDTO furnitureDTO;
    private Carpenter carpenter;

    @BeforeEach
    void setUp() {

        carpenter = new Carpenter();
        carpenter.setCarpenterId(1L);

        request = new RequestFurniture();
        request.setName("Mesa");
        request.setCreationDate("2026-03-07");
        request.setEndDate("2026-03-09");
        request.setStatus(FurnitureStatus.PENDING);
        request.setType(FurnitureType.MESA);
        request.setCarpenterId(1L);

        furnitureDTO = new FurnitureDTO();
        furnitureDTO.setName("Mesa");
        furnitureDTO.setCarpenterId(1L);

        furniture = new Furniture();
        furniture.setFurnitureId(1L);
        furniture.setName("Mesa");
        furniture.setCreationDate(LocalDate.now());
    }

    @Test
    void createFurnitureSuccess() {
        //Arrange
        when(furnitureMapper.toDTO(request)).thenReturn(furnitureDTO);
        when(furnitureMapper.toEntity(furnitureDTO)).thenReturn(furniture);
        when(carpenterRepository.findById(1L)).thenReturn(Optional.of(carpenter));
        when(furnitureRepository.save(any())).thenReturn(furniture);
        when(furnitureMapper.toDTO(furniture)).thenReturn(furnitureDTO);

        //Act
        FurnitureDTO result = furnitureService.createFurniture(request);

        //Assert
        assertNotNull(result);
        verify(furnitureRepository).save(any());
    }

    @Test
    void createFurnitureWithoutName() {
        //Arrange
        request.setName(null);

        //Act & Assert
        assertThrows(IllegalArgumentException.class, () -> furnitureService.createFurniture(request));
        verify(furnitureRepository, never()).save(any());
    }

    @Test
    void createFurnitureWithoutCreationDate() {
        //Arrange
        request.setCreationDate(null);

        //Act & Assert
        assertThrows(IllegalArgumentException.class, () -> furnitureService.createFurniture(request));
        verify(furnitureRepository, never()).save(any());
    }

    @Test
    void createFurnitureWithoutStatus() {
        //Arrange
        request.setStatus(null);

        //Act & Assert
        assertThrows(IllegalArgumentException.class, () -> furnitureService.createFurniture(request));
        verify(furnitureRepository, never()).save(any());
    }

    @Test
    void createFurnitureWithoutType() {
        //Arrange
        request.setType(null);

        //Act & Assert
        assertThrows(IllegalArgumentException.class, () -> furnitureService.createFurniture(request));
        verify(furnitureRepository, never()).save(any());
    }

    @Test
    void createFurnitureWithoutCarpenterId() {
        //Arrange
        request.setCarpenterId(null);

        //Act & Assert
        assertThrows(IllegalArgumentException.class, () -> furnitureService.createFurniture(request));
        verify(furnitureRepository, never()).save(any());
    }

    @Test
    void createFurnitureCarpenterNotFound() {
        //Arrange
        when(furnitureMapper.toDTO(request)).thenReturn(furnitureDTO);
        when(furnitureMapper.toEntity(furnitureDTO)).thenReturn(furniture);
        when(carpenterRepository.findById(1L)).thenReturn(Optional.empty());

        //Act & Assert
        assertThrows(EntityNotFoundException.class, () -> furnitureService.createFurniture(request));
        verify(furnitureRepository, never()).save(any());
    }

    @Test
    void getFurnitureByCarpenter() {
        //Arrange
        when(carpenterRepository.findById(1L)).thenReturn(Optional.of(carpenter));
        when(furnitureRepository.findByCarpenter(carpenter)).thenReturn(List.of(furniture));
        when(furnitureMapper.toDTO(furniture)).thenReturn(furnitureDTO);

        //Act
        List<FurnitureDTO> result = furnitureService.getFurnituresByCarpenterId(1L);

        //Assert
        assertEquals(1, result.size());
        verify(furnitureRepository).findByCarpenter(carpenter);
    }

    @Test
    void getFurnitureByCarpenterNotFound() {
        //Arrange
        when(carpenterRepository.findById(1L)).thenReturn(Optional.empty());

        //Act & Assert
        assertThrows(EntityNotFoundException.class, () -> furnitureService.getFurnituresByCarpenterId(1L));
    }

    @Test
    void getFurnitureByTokenSuccess() {
        //Arrange
        String token = "token";

        when(jwtUtil.extractEmail(token)).thenReturn("getByToken@email.com");
        when(carpenterRepository.findByEmail("getByToken@email.com")).thenReturn(Optional.of(carpenter));
        when(furnitureRepository.findFurnitureByCarpenterId(1L)).thenReturn(List.of(furniture));
        when(furnitureMapper.toDTO(furniture)).thenReturn(furnitureDTO);

        //Act
        List<FurnitureDTO> result = furnitureService.getFurnitureByToken(token);

        //Assert
        assertEquals(1, result.size());
    }

    @Test
    void getFurnitureByTokenWithoutToken() {
        //Act & Assert
        assertThrows(IllegalArgumentException.class, () -> furnitureService.getFurnitureByToken(null));
    }

    @Test
    void updateFurnitureSuccess() {
        //Arrange
        when(furnitureRepository.findById(1L)).thenReturn(Optional.of(furniture));
        when(carpenterRepository.findById(1L)).thenReturn(Optional.of(carpenter));
        request.setName("Mesa Actualizada");
        when(furnitureRepository.save(any())).thenReturn(furniture);
        when(furnitureMapper.toDTO(furniture)).thenReturn(furnitureDTO);

        //Act
        FurnitureDTO result = furnitureService.updateFurniture(1L, request);

        //Assert
        assertNotNull(result);
        assertEquals("Mesa Actualizada", furniture.getName());
        verify(furnitureRepository).save(furniture);
    }

    @Test
    void updateFurnitureNotFound() {
        //Arrange
        when(furnitureRepository.findById(1L)).thenReturn(Optional.empty());

        //Act & Assert
        assertThrows(EntityNotFoundException.class, () -> furnitureService.updateFurniture(1L, request));
    }

    @Test
    void updateFurnitureWithoutName() {
        //Arrange
        when(furnitureRepository.findById(1L)).thenReturn(Optional.of(furniture));
        request.setName(null);

        //Act & Assert
        assertThrows(IllegalArgumentException.class, () -> furnitureService.updateFurniture(1L, request));
    }

    @Test
    void updateFurnitureWithoutType() {
        //Arrange
        when(furnitureRepository.findById(1L)).thenReturn(Optional.of(furniture));
        request.setType(null);

        //Act & Assert
        assertThrows(IllegalArgumentException.class, () -> furnitureService.updateFurniture(1L, request));
    }
}

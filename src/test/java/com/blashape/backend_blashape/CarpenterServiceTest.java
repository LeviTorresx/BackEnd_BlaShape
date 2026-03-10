package com.blashape.backend_blashape;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.blashape.backend_blashape.DTOs.CarpenterDTO;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.entitys.UserRole;
import com.blashape.backend_blashape.mapper.CarpenterMapper;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.services.CarpenterService;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class CarpenterServiceTest {
    @Mock
    private CarpenterRepository carpenterRepository;

    @Mock
    private CarpenterMapper carpenterMapper;

    @InjectMocks
    private CarpenterService carpenterService;

    private Carpenter carpenter;
    private CarpenterDTO carpenterDTO;

    @BeforeEach
    void setUp() {
        carpenter = new Carpenter();
        carpenter.setCarpenterId(1L);
        carpenter.setName("Jimmy");
        carpenter.setLastName("Torres");
        carpenter.setDni("12345678");
        carpenter.setRut("12345678-9");
        carpenter.setEmail("jimmy.torres@gmail.com");
        carpenter.setPassword("password");
        carpenter.setPhone("1234567890");
        carpenter.setRole(UserRole.CARPENTER);
        
        carpenterDTO = new CarpenterDTO();
        carpenterDTO.setCarpenterId(1L);
        carpenterDTO.setName("Jimmy");
        carpenterDTO.setLastName("Torres");
        carpenterDTO.setDni("12345678");
        carpenterDTO.setRut("12345678-9");
        carpenterDTO.setEmail("jimmy.torres@gmail.com");
        carpenterDTO.setPassword("password");
        carpenterDTO.setPhone("1234567890");
        carpenterDTO.setRole("CARPENTER");
    }

    @Test
    void getCarpenterById() {
        //Arrange
        when(carpenterRepository.findById(1L)).thenReturn(Optional.of(carpenter));
        when(carpenterMapper.toDTO(carpenter)).thenReturn(carpenterDTO);

        //Act
        CarpenterDTO result = carpenterService.getCarpenterById(1L);

        //Assert
        assertNotNull(result);
        assertEquals("Jimmy", result.getName());

        verify(carpenterRepository).findById(1L);
        verify(carpenterMapper).toDTO(carpenter);
    }

    @Test
    void getCarpenterByIdNotFound() {
        //Arrange
        when(carpenterRepository.findById(1L)).thenReturn(Optional.empty());

        //Act & Assert
        assertThrows(EntityNotFoundException.class, () -> carpenterService.getCarpenterById(1L));
    }

    @Test
    void getAllCarpenters() {
        //Arrange
        when(carpenterRepository.findAll()).thenReturn(List.of(carpenter));
        when(carpenterMapper.toDTO(carpenter)).thenReturn(carpenterDTO);

        //Act
        List<CarpenterDTO> result = carpenterService.getAllCarpenters();

        //Assert
        assertEquals(1, result.size());
        assertEquals("Jimmy", result.get(0).getName());

        verify(carpenterRepository).findAll();
        verify(carpenterMapper).toDTO(carpenter);
    }

    @Test
    void updateCarpenter() {
        //Arrange
        when(carpenterRepository.findById(1L)).thenReturn(Optional.of(carpenter));
        when(carpenterRepository.save(any(Carpenter.class))).thenReturn(carpenter);
        when(carpenterMapper.toDTO(carpenter)).thenReturn(carpenterDTO);

        carpenterDTO.setName("Joselito");

        //Act
        CarpenterDTO result = carpenterService.updateCarpenter(1L, carpenterDTO);

        //Assert
        assertNotNull(result);
        assertEquals("Joselito", result.getName());

        verify(carpenterRepository).findById(1L);
        verify(carpenterRepository).save(carpenter);
        verify(carpenterMapper).toDTO(carpenter);
    }

    @Test
    void updateCarpenterWithInvalidEmailFormat() {
        //Arrange
        carpenterDTO.setEmail("correo.invalido.com");

        when(carpenterRepository.findById(1L)).thenReturn(Optional.of(carpenter));

        //Act & Assert
        assertThrows(IllegalArgumentException.class, () -> carpenterService.updateCarpenter(1L, carpenterDTO));
    }

     @Test
     void updateCarpenterWithExistingEmail() {
         //Arrange
         carpenterDTO.setEmail("existente@gmail.com");

         when(carpenterRepository.findById(1L)).thenReturn(Optional.of(carpenter));
         when(carpenterRepository.existsByEmail("existente@gmail.com")).thenReturn(true);

        //Act & Assert
        assertThrows(EntityExistsException.class, () -> carpenterService.updateCarpenter(1L, carpenterDTO));
     }

     @Test
     void deleteCarpenter() {
         //Arrange
         when(carpenterRepository.existsById(1L)).thenReturn(true);

         //Act
         carpenterService.deleteCarpenter(1L);

         //Assert
         verify(carpenterRepository).deleteById(1L);
     }

     @Test
     void deleteCarpenterNotFound() {
         //Arrange
         when(carpenterRepository.existsById(1L)).thenReturn(false);

         //Act & Assert
         assertThrows(EntityNotFoundException.class, () -> carpenterService.deleteCarpenter(1L));
     }
}

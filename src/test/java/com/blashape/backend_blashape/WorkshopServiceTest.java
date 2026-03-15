package com.blashape.backend_blashape;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.blashape.backend_blashape.DTOs.WorkshopDTO;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.entitys.Workshop;
import com.blashape.backend_blashape.mapper.WorkshopMapper;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.repositories.WorkshopRepository;
import com.blashape.backend_blashape.services.WorkshopService;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkshopServiceTest {

    @Mock
    private WorkshopRepository workshopRepository;

    @Mock
    private CarpenterRepository carpenterRepository;

    @Mock
    private WorkshopMapper workshopMapper;

    @InjectMocks
    private WorkshopService workshopService;

    private Workshop workshop;
    private WorkshopDTO workshopDTO;
    private Carpenter carpenter;

    @BeforeEach
    void setUp(){

        carpenter = new Carpenter();
        carpenter.setCarpenterId(1L);

        workshop = new Workshop();
        workshop.setWorkshopId(1L);
        workshop.setName("Taller Central");
        workshop.setAddress("Calle 123");

        workshopDTO = new WorkshopDTO();
        workshopDTO.setWorkshopId(1L);
        workshopDTO.setName("Taller Central");
        workshopDTO.setAddress("Calle 123");
        workshopDTO.setPhone("3000000000");
        workshopDTO.setCarpenterId(1L);
    }

    // CREATE SUCCESS
    @Test
    void createWorkshopSuccess(){

        when(workshopMapper.toEntity(workshopDTO)).thenReturn(workshop);
        when(carpenterRepository.findById(1L)).thenReturn(Optional.of(carpenter));
        when(workshopRepository.save(any())).thenReturn(workshop);
        when(workshopMapper.toDto(workshop)).thenReturn(workshopDTO);

        WorkshopDTO result = workshopService.createWorkshop(workshopDTO);

        assertNotNull(result);
        verify(workshopRepository).save(workshop);
    }

    // CREATE WITHOUT NAME
    @Test
    void createWorkshopWithoutName(){

        workshopDTO.setName(null);

        assertThrows(IllegalArgumentException.class,
                () -> workshopService.createWorkshop(workshopDTO));

        verify(workshopRepository, never()).save(any());
    }

    // CREATE WITHOUT ADDRESS
    @Test
    void createWorkshopWithoutAddress(){

        workshopDTO.setAddress(null);

        assertThrows(IllegalArgumentException.class,
                () -> workshopService.createWorkshop(workshopDTO));
    }

    // CREATE WITHOUT PHONE
    @Test
    void createWorkshopWithoutPhone(){

        workshopDTO.setPhone(null);

        assertThrows(IllegalArgumentException.class,
                () -> workshopService.createWorkshop(workshopDTO));
    }

    // CREATE WITHOUT CARPENTER
    @Test
    void createWorkshopWithoutCarpenter(){

        workshopDTO.setCarpenterId(null);

        assertThrows(IllegalArgumentException.class,
                () -> workshopService.createWorkshop(workshopDTO));
    }

    // CREATE DUPLICATE WORKSHOP
    @Test
    void createDuplicateWorkshopShouldFail() {

        workshopDTO.setNit("123456789");

        when(workshopRepository.existsByNit("123456789"))
                .thenReturn(false) // primera creación
                .thenReturn(true); // segunda creación

        when(carpenterRepository.findById(1L)).thenReturn(Optional.of(carpenter));
        when(workshopMapper.toEntity(workshopDTO)).thenReturn(workshop);
        when(workshopRepository.save(any())).thenReturn(workshop);
        when(workshopMapper.toDto(workshop)).thenReturn(workshopDTO);

        // primera creación
        workshopService.createWorkshop(workshopDTO);

        // segunda creación debe fallar
        assertThrows(IllegalArgumentException.class,
                () -> workshopService.createWorkshop(workshopDTO));

        verify(workshopRepository, times(1)).save(any());
    }

    // CARPENTER NOT FOUND
    @Test
    void createWorkshopCarpenterNotFound(){

        when(workshopMapper.toEntity(workshopDTO)).thenReturn(workshop);
        when(carpenterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> workshopService.createWorkshop(workshopDTO));
    }

    // GET WORKSHOP SUCCESS
    @Test
    void getWorkshopSuccess(){

        when(workshopRepository.findById(1L)).thenReturn(Optional.of(workshop));
        when(workshopMapper.toDto(workshop)).thenReturn(workshopDTO);

        WorkshopDTO result = workshopService.getWorkshop(1L);

        assertNotNull(result);
        assertEquals("Taller Central", result.getName());
    }

    // GET WORKSHOP NOT FOUND
    @Test
    void getWorkshopNotFound(){

        when(workshopRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> workshopService.getWorkshop(1L));
    }

    // GET BY CARPENTER SUCCESS
    @Test
    void getWorkshopByCarpenterSuccess(){

        carpenter.setWorkshop(workshop);

        when(carpenterRepository.findById(1L)).thenReturn(Optional.of(carpenter));
        when(workshopMapper.toDto(workshop)).thenReturn(workshopDTO);

        WorkshopDTO result = workshopService.getWorkshopByCarpenterId(1L);

        assertNotNull(result);
    }

    // GET BY CARPENTER NOT FOUND
    @Test
    void getWorkshopByCarpenterNotFound(){

        when(carpenterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> workshopService.getWorkshopByCarpenterId(1L));
    }

    // UPDATE SUCCESS
    @Test
    void updateWorkshopSuccess(){

        when(workshopRepository.findById(1L)).thenReturn(Optional.of(workshop));
        when(workshopRepository.save(any())).thenReturn(workshop);
        when(workshopMapper.toDto(workshop)).thenReturn(workshopDTO);

        workshopDTO.setName("Taller Actualizado");

        WorkshopDTO result = workshopService.updateWorkshop(1L, workshopDTO);

        assertNotNull(result);
        verify(workshopRepository).save(workshop);
    }

    // UPDATE NOT FOUND
    @Test
    void updateWorkshopNotFound(){

        when(workshopRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> workshopService.updateWorkshop(1L, workshopDTO));
    }

    // DELETE SUCCESS
    @Test
    void deleteWorkshopSuccess(){

        workshop.setCarpenter(carpenter);

        when(workshopRepository.findById(1L)).thenReturn(Optional.of(workshop));

        workshopService.deleteWorkshop(1L);

        verify(workshopRepository).delete(workshop);
        verify(carpenterRepository).save(carpenter);
    }

}
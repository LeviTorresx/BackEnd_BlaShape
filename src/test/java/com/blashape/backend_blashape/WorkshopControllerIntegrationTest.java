package com.blashape.backend_blashape;

import com.blashape.backend_blashape.DTOs.WorkshopDTO;
import com.blashape.backend_blashape.DTOs.WorkshopResponse;
import com.blashape.backend_blashape.controllers.WorkshopController;
import com.blashape.backend_blashape.controllers.GlobalExceptionHandler;
import com.blashape.backend_blashape.services.WorkshopService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WorkshopControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private WorkshopService workshopService;

    @InjectMocks
    private WorkshopController workshopController;

    @BeforeEach
    void setup() {

        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders
                .standaloneSetup(workshopController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // CREATE
    @Test
    void createWorkshop() throws Exception {

        WorkshopDTO dto = new WorkshopDTO();
        dto.setName("Taller Central");

        when(workshopService.createWorkshop(any())).thenReturn(dto);

        String json = """
        {
            "name":"Taller Central",
            "address":"Calle 123",
            "phone":"3000000000",
            "carpenterId":1
        }
        """;

        mockMvc.perform(post("/api_BS/workshop/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Taller Taller Central creado correctamente"));
    }

    // CREATE DUPLICATE WORKSHOP REQUEST
    @Test
    void createDuplicateWorkshopRequestShouldFail() throws Exception {

        WorkshopDTO dto = new WorkshopDTO();
        dto.setName("Taller Central");

        when(workshopService.createWorkshop(any()))
                .thenReturn(dto)
                .thenThrow(new IllegalArgumentException("Ya existe un taller con ese NIT"));

        String json = """
        {
            "name":"Taller Central",
            "address":"Calle 123",
            "phone":"3000000000",
            "nit":"123456789",
            "carpenterId":1
        }
        """;

        mockMvc.perform(post("/api_BS/workshop/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api_BS/workshop/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Ya existe un taller con ese NIT"));

        verify(workshopService, times(2)).createWorkshop(any());
    }

    // GET BY ID
    @Test
    void getWorkshop() throws Exception {

        WorkshopDTO dto = new WorkshopDTO();
        dto.setName("Taller Central");

        when(workshopService.getWorkshop(1L)).thenReturn(dto);

        mockMvc.perform(get("/api_BS/workshop/get/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Taller Central"));
    }

    // GET BY CARPENTER
    @Test
    void getWorkshopByCarpenter() throws Exception {

        WorkshopDTO dto = new WorkshopDTO();
        dto.setName("Taller Central");

        when(workshopService.getWorkshopByCarpenterId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api_BS/workshop/by-carpenter/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Taller Central"));
    }

    // UPDATE
    @Test
    void updateWorkshop() throws Exception {

        WorkshopDTO dto = new WorkshopDTO();
        dto.setName("Taller Actualizado");

        when(workshopService.updateWorkshop(eq(1L), any())).thenReturn(dto);

        String json = """
        {
            "name":"Taller Actualizado",
            "address":"Calle 123",
            "phone":"3000000000"
        }
        """;

        mockMvc.perform(put("/api_BS/workshop/edit/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Taller Taller Actualizado actualizado exitosamente"));
    }

    // DELETE
    @Test
    void deleteWorkshop() throws Exception {

        mockMvc.perform(delete("/api_BS/workshop/delete/1"))
                .andExpect(status().isNoContent());
    }

}
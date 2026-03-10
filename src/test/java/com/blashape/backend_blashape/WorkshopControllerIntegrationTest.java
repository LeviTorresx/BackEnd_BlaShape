package com.blashape.backend_blashape;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.blashape.backend_blashape.DTOs.WorkshopDTO;
import com.blashape.backend_blashape.DTOs.WorkshopResponse;
import com.blashape.backend_blashape.services.WorkshopService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkshopControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkshopService workshopService;

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

        WorkshopResponse response = new WorkshopResponse(
                "Taller Taller Actualizado actualizado exitosamente",
                dto
        );

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
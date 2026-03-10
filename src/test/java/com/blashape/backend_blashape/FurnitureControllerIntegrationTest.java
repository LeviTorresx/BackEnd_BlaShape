package com.blashape.backend_blashape;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.blashape.backend_blashape.DTOs.FurnitureDTO;
import com.blashape.backend_blashape.services.FurnitureService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FurnitureControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FurnitureService furnitureService;

    @Test
    void createFurniture() throws Exception {
        FurnitureDTO dto = new FurnitureDTO();
        dto.setName("Mesa");

        when(furnitureService.createFurniture(any())).thenReturn(dto);

        String jsonData = """
        {
            "name":"Mesa",
            "creationDate":"2026-03-07",
            "status":"PENDING",
            "type":"MESA",
            "carpenterId":1
        }
        """;

        MockMultipartFile imageInit = new MockMultipartFile("imageInit", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "test-image".getBytes());

        mockMvc.perform(multipart("/api_BS/furniture/create")
                    .file(imageInit)
                    .param("data", jsonData)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                    .value("Mueble Mesa creado exitosamente"));
    }

    @Test
    void getFurnitureByCarpenter() throws Exception {
        FurnitureDTO dto = new FurnitureDTO();
        dto.setFurnitureId(1L);
        dto.setName("Mesa");
        dto.setCreationDate(LocalDate.now());

        when(furnitureService.getFurnituresByCarpenterId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api_BS/furniture/by-carpenter/1")).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Mesa"));
    }

    @Test
    void getFurnitureByToken() throws Exception {
        FurnitureDTO dto = new FurnitureDTO();
        dto.setName("Mesa");

        when(furnitureService.getFurnitureByToken("tokentest")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api_BS/furniture/all")
                        .cookie(new jakarta.servlet.http.Cookie("jwt","tokentest")))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].name").value("Mesa"));
    }

    @Test
    void getFurnitureByTokenWithoutCookie() throws Exception {
        mockMvc.perform(get("/api_BS/furniture/all")).andExpect(status().isUnauthorized());
    }

    @Test
    void updateFurniture() throws Exception {
        FurnitureDTO dto = new FurnitureDTO();
        dto.setName("Mesa Actualizada");

        when(furnitureService.updateFurniture(eq(1L), any())).thenReturn(dto);

        String jsonData = """
        {
            "name":"Mesa Actualizada",
            "creationDate":"2026-03-07",
            "status":"PENDING",
            "type":"MESA",
            "carpenterId":1
        }
        """;

        MockMultipartFile imageInit = new MockMultipartFile("imageInit", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "test-image".getBytes());

        mockMvc.perform(multipart("/api_BS/furniture/edit/1")
                        .file(imageInit)
                        .param("data", jsonData)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.message")
                        .value("Mueble Mesa Actualizada actualizado exitosamente"));
    }
}

package com.blashape.backend_blashape;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.blashape.backend_blashape.DTOs.CustomerDTO;
import com.blashape.backend_blashape.services.CustomerService;

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
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    // CREATE CUSTOMER
    @Test
    void createCustomer() throws Exception {

        CustomerDTO dto = new CustomerDTO();
        dto.setName("Deibinson");

        when(customerService.createCustomer(any())).thenReturn(dto);

        String json = """
        {
            "name":"Deibinson",
            "lastName":"Perez",
            "dni":"123456",
            "phone":"3000000000",
            "email":"mono@email.com",
            "carpenterId":1
        }
        """;

        mockMvc.perform(post("/api_BS/customer/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Cliente Deibinson creado exitosamente"));
    }

    // GET CUSTOMER BY ID
    @Test
    void getCustomer() throws Exception {

        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerId(1L);
        dto.setName("Deibinson");

        when(customerService.getCustomer(1L)).thenReturn(dto);

        mockMvc.perform(get("/api_BS/customer/get/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Deibinson"));
    }

    // GET ALL BY TOKEN
    @Test
    void getCustomersByToken() throws Exception {

        CustomerDTO dto = new CustomerDTO();
        dto.setName("Deibinson");

        when(customerService.getCustomersByToken("tokentest"))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api_BS/customer/all")
                        .cookie(new jakarta.servlet.http.Cookie("jwt","tokentest")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Deibinson"));
    }

    // GET ALL WITHOUT TOKEN
    @Test
    void getCustomersWithoutToken() throws Exception {

        mockMvc.perform(get("/api_BS/customer/all"))
                .andExpect(status().isUnauthorized());
    }

    // UPDATE CUSTOMER
    @Test
    void updateCustomer() throws Exception {

        CustomerDTO dto = new CustomerDTO();
        dto.setName("Monooo");

        when(customerService.updateCustomer(eq(1L), any()))
                .thenReturn(dto);

        String json = """
        {
            "name":"Monooo",
            "lastName":"Perez",
            "dni":"123456",
            "phone":"3000000000",
            "email":"trosquinality132@email.com",
            "carpenterId":1
        }
        """;

        mockMvc.perform(put("/api_BS/customer/edit/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Monooo"));
    }

    // DELETE CUSTOMER
    @Test
    void deleteCustomer() throws Exception {

        mockMvc.perform(delete("/api_BS/customer/delete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Cliente eliminado correctamente"));
    }

}
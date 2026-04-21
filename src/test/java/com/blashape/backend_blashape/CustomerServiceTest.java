package com.blashape.backend_blashape;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.blashape.backend_blashape.DTOs.CustomerDTO;
import com.blashape.backend_blashape.config.JwtUtil;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.entitys.Customer;
import com.blashape.backend_blashape.entitys.UserRole;
import com.blashape.backend_blashape.mapper.CustomerMapper;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.repositories.CustomerRepository;
import com.blashape.backend_blashape.repositories.FurnitureRepository;
import com.blashape.backend_blashape.services.CustomerService;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private FurnitureRepository furnitureRepository;

    @Mock
    private CarpenterRepository carpenterRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private CustomerDTO customerDTO;
    private Customer customer;
    private Carpenter carpenter;

    @BeforeEach
    void setUp() {

        carpenter = new Carpenter();
        carpenter.setCarpenterId(1L);

        customerDTO = new CustomerDTO();
        customerDTO.setCustomerId(1L);
        customerDTO.setName("Deibinson");
        customerDTO.setLastName("Perez");
        customerDTO.setDni("123456");
        customerDTO.setPhone("3000000000");
        customerDTO.setEmail("deibinson@email.com");
        customerDTO.getCarpenterIds().add(1L);

        customer = new Customer();
        customer.setCustomerId(1L);
        customer.setName("Levi");
        customer.setRole(UserRole.DEFAULT);
    }

    // CREATE CUSTOMER SUCCESS
    @Test
    void createCustomerSuccess() {

        when(carpenterRepository.findById(1L)).thenReturn(Optional.of(carpenter));
        when(customerMapper.toEntity(customerDTO)).thenReturn(customer);
        when(customerRepository.save(any())).thenReturn(customer);
        when(customerMapper.toDTO(customer)).thenReturn(customerDTO);

        CustomerDTO result = customerService.createCustomer(customerDTO);

        assertNotNull(result);
        verify(customerRepository).save(any());
    }

    // NAME NULL
    @Test
    void createCustomerWithoutName() {

        customerDTO.setName(null);

        assertThrows(IllegalArgumentException.class,
                () -> customerService.createCustomer(customerDTO));

        verify(customerRepository, never()).save(any());
    }

    // DNI NULL
    @Test
    void createCustomerWithoutDni() {

        customerDTO.setDni(null);

        assertThrows(IllegalArgumentException.class,
                () -> customerService.createCustomer(customerDTO));
    }

    // EMAIL INVALID
    @Test
    void createCustomerInvalidEmail() {

        customerDTO.setEmail("correo-invalido");

        assertThrows(IllegalArgumentException.class,
                () -> customerService.createCustomer(customerDTO));
    }

    // PHONE NULL
    @Test
    void createCustomerWithoutPhone() {

        customerDTO.setPhone(null);

        assertThrows(IllegalArgumentException.class,
                () -> customerService.createCustomer(customerDTO));
    }

    // CARPENTER NULL
    @Test
    void createCustomerWithoutCarpenter() {

        customerDTO.getCarpenterIds().clear();

        assertThrows(IllegalArgumentException.class,
                () -> customerService.createCustomer(customerDTO));
    }

    // CARPENTER NOT FOUND
    @Test
    void createCustomerCarpenterNotFound() {

        when(carpenterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> customerService.createCustomer(customerDTO));

        verify(customerRepository, never()).save(any());
    }

    // GET CUSTOMER SUCCESS
    @Test
    void getCustomerSuccess() {

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerMapper.toDTO(customer)).thenReturn(customerDTO);

        CustomerDTO result = customerService.getCustomer(1L);

        assertNotNull(result);
        assertEquals("Deibinson", result.getName());
    }

    // GET CUSTOMER NOT FOUND
    @Test
    void getCustomerNotFound() {

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> customerService.getCustomer(1L));
    }

    // GET CUSTOMERS BY TOKEN SUCCESS
    @Test
    void getCustomersByTokenSuccess() {

        String token = "token";

        when(jwtUtil.extractEmail(token)).thenReturn("carpenter@email.com");
        when(carpenterRepository.findByEmail("carpenter@email.com")).thenReturn(Optional.of(carpenter));
        when(customerRepository.findActiveCustomersByCarpenterId(1L)).thenReturn(List.of(customer));
        when(customerMapper.toDTO(customer)).thenReturn(customerDTO);

        List<CustomerDTO> result = customerService.getCustomersByToken(token);

        assertEquals(1, result.size());
    }

    // TOKEN NULL
    @Test
    void getCustomersByTokenWithoutToken() {

        assertThrows(IllegalArgumentException.class,
                () -> customerService.getCustomersByToken(null));
    }

    // UPDATE SUCCESS
    @Test
    void updateCustomerSuccess() {

        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(carpenterRepository.findById(1L)).thenReturn(Optional.of(carpenter));
        when(customerRepository.save(any())).thenReturn(customer);
        when(customerMapper.toDTO(customer)).thenReturn(customerDTO);

        customerDTO.setName("Monooo");
        customerDTO.getCarpenterIds().add(1L);

        // Act
        CustomerDTO result = customerService.updateCustomer(1L, customerDTO);

        // Assert
        assertNotNull(result);
        verify(customerRepository).save(customer);
    }

    // UPDATE CUSTOMER NOT FOUND
    @Test
    void updateCustomerNotFound() {

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> customerService.updateCustomer(1L, customerDTO));
    }

    // DELETE CUSTOMER SUCCESS
    @Test
    void deleteCustomerSuccess() {

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(1L);

        assertTrue(!customer.getIsActive());
        assertNotNull(customer.getDeletedAt());
        verify(customerRepository).save(customer);
    }

    // DELETE CUSTOMER NOT FOUND
    @Test
    void deleteCustomerNotFound() {

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> customerService.deleteCustomer(1L));
    }

    // CREATE DUPLICATE CUSTOMER
    @Test
    void createDuplicateCustomerShouldFail() {

        when(customerRepository.existsByDni("123456"))
                .thenReturn(false)   // primera llamada
                .thenReturn(true);   // segunda llamada

        when(customerRepository.existsByEmail("deibinson@email.com"))
                .thenReturn(false);

        when(customerRepository.existsByPhone("3000000000"))
                .thenReturn(false);

        when(carpenterRepository.findById(1L)).thenReturn(Optional.of(carpenter));
        when(customerMapper.toEntity(customerDTO)).thenReturn(customer);
        when(customerRepository.save(any())).thenReturn(customer);
        when(customerMapper.toDTO(customer)).thenReturn(customerDTO);

        // primera creación (debe funcionar)
        customerService.createCustomer(customerDTO);

        // segunda creación (debe fallar)
        assertThrows(IllegalArgumentException.class,
                () -> customerService.createCustomer(customerDTO));
    }
}
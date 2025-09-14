package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.CustomerDTO;
import com.blashape.backend_blashape.entitys.Customer;
import com.blashape.backend_blashape.entitys.Furniture;
import com.blashape.backend_blashape.entitys.UserRole;
import com.blashape.backend_blashape.repositories.CustomerRepository;
import com.blashape.backend_blashape.repositories.FurnitureRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final FurnitureRepository furnitureRepository;
    private final ObjectMapper objectMapper;
    
    public CustomerDTO createCustomer(CustomerDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        if (dto.getPhone() == null || dto.getPhone().isBlank()) {
            throw new IllegalArgumentException("El teléfono del cliente es obligatorio");
        }

        Customer customer = objectMapper.convertValue(dto, Customer.class);

        // Si vienen muebles asociados
        if (dto.getFurnitureListIds() != null && !dto.getFurnitureListIds().isEmpty()) {
            List<Furniture> furnitureList = furnitureRepository.findAllById(dto.getFurnitureListIds());
            customer.setFurnitureList(furnitureList);
        }

        customer.setRole(UserRole.DEFAULT);

        Customer saved = customerRepository.save(customer);

        return mapToDTO(saved);
    }

    public CustomerDTO getCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + customerId));
        return mapToDTO(customer);
    }

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    public CustomerDTO updateCustomer(Long customerId, CustomerDTO dto) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + customerId));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            customer.setName(dto.getName());
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            customer.setLastName(dto.getLastName());
        }
        if (dto.getDni() != null && !dto.getDni().isBlank()) {
            customer.setDni(dto.getDni());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            customer.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            customer.setEmail(dto.getEmail());
        }

        // Actualizar muebles si llegan nuevos IDs
        if (dto.getFurnitureListIds() != null) {
            List<Furniture> furnitureList = furnitureRepository.findAllById(dto.getFurnitureListIds());
            customer.setFurnitureList(furnitureList);
        }

        Customer updated = customerRepository.save(customer);
        return mapToDTO(updated);
    }

    public void deleteCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new EntityNotFoundException("Cliente no encontrado con ID: " + customerId);
        }
        customerRepository.deleteById(customerId);
    }

    private CustomerDTO mapToDTO(Customer customer) {
        CustomerDTO dto = objectMapper.convertValue(customer, CustomerDTO.class);

        if (customer.getFurnitureList() != null) {
            dto.setFurnitureListIds(
                    customer.getFurnitureList().stream()
                            .map(Furniture::getFurnitureId)
                            .toList()
            );
        }
        return dto;
    }
}


package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.CustomerDTO;
import com.blashape.backend_blashape.entitys.Customer;
import com.blashape.backend_blashape.entitys.Furniture;
import com.blashape.backend_blashape.entitys.UserRole;
import com.blashape.backend_blashape.mapper.CustomerMapper;
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
    private final CustomerMapper customerMapper;
    
    public CustomerDTO createCustomer(CustomerDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }

        if (dto.getDni() == null || dto.getDni().isBlank()){
            throw new IllegalArgumentException("La cedula del cliente es obligatoria");
        }

        if(dto.getEmail() == null || dto.getEmail().isBlank()){
            throw new IllegalArgumentException("El correo del cliente es obligatorio");
        }

        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("El formato del correo es inválido");
        }

        if (dto.getPhone() == null || dto.getPhone().isBlank()) {
            throw new IllegalArgumentException("El teléfono del cliente es obligatorio");
        }

        Customer customer = customerMapper.toEntity(dto);

        // Si vienen muebles asociados
        if (dto.getFurnitureListIds() != null && !dto.getFurnitureListIds().isEmpty()) {
            List<Furniture> furnitureList = furnitureRepository.findAllById(dto.getFurnitureListIds());
            customer.setFurnitureList(furnitureList);
        }

        customer.setRole(UserRole.DEFAULT);

        Customer saved = customerRepository.save(customer);

        return customerMapper.toDTO(saved);
    }

    public CustomerDTO getCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + customerId));
        return customerMapper.toDTO(customer);
    }

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toDTO)
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

        Customer updated = customerRepository.save(customer);
        return customerMapper.toDTO(updated);
    }

    public void deleteCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new EntityNotFoundException("Cliente no encontrado con ID: " + customerId);
        }
        customerRepository.deleteById(customerId);
    }
}


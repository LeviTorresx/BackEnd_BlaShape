package com.blashape.backend_blashape.services;
import com.blashape.backend_blashape.DTOs.CarpenterDTO;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.entitys.Workshop;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.repositories.WorkshopRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarpenterService {
    private CarpenterRepository carpenterRepository;
    private WorkshopRepository workshopRepository;
    private ObjectMapper  objectMapper;
    private PasswordEncoder passwordEncoder;

    public CarpenterDTO createCarpenter(CarpenterDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (dto.getLastName() == null || dto.getLastName().isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        if (dto.getIdNumber() == null || dto.getIdNumber().isBlank()) {
            throw new IllegalArgumentException("La cédula es obligatoria");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("El formato del correo es inválido");
        }

        if (dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        if (carpenterRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }
        if (carpenterRepository.existsByIdNumber(dto.getIdNumber())) {
            throw new IllegalArgumentException("La cédula ya está registrada");
        }

        Carpenter carpenter = objectMapper.convertValue(dto, Carpenter.class);

        // 🔐 Encriptar contraseña
        carpenter.setPassword(passwordEncoder.encode(dto.getPassword()));

        carpenter.setWorkshop(null);

        Carpenter saved = carpenterRepository.save(carpenter);

        CarpenterDTO response = objectMapper.convertValue(saved, CarpenterDTO.class);
        response.setWorkshopId(null);
        response.setPassword(null);
        return response;
    }

    public CarpenterDTO getCarpenterById(Long id) {
        Carpenter carpenter = carpenterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado con ID: " + id));

        CarpenterDTO dto = objectMapper.convertValue(carpenter, CarpenterDTO.class);
        dto.setWorkshopId(carpenter.getWorkshop() != null ? carpenter.getWorkshop().getWorkshopId() : null);
        return dto;
    }

    public List<CarpenterDTO> getAllCarpenters() {
        List<Carpenter> carpenters = carpenterRepository.findAll();

        return carpenters.stream()
                .map(carpenter -> {
                    CarpenterDTO dto = objectMapper.convertValue(carpenter, CarpenterDTO.class);
                    dto.setWorkshopId(carpenter.getWorkshop() != null ? carpenter.getWorkshop().getWorkshopId() : null);
                    return dto;
                })
                .toList();
    }

    public CarpenterDTO updateCarpenter(Long id, CarpenterDTO dto) {
        Carpenter existing = carpenterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado con ID: " + id));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            existing.setName(dto.getName());
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            existing.setLastName(dto.getLastName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                throw new IllegalArgumentException("El formato del correo es inválido");
            }
            if (!dto.getEmail().equals(existing.getEmail()) && carpenterRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("El correo ya está en uso");
            }
            existing.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            existing.setPhone(dto.getPhone());
        }

        Carpenter updated = carpenterRepository.save(existing);

        CarpenterDTO response = objectMapper.convertValue(updated, CarpenterDTO.class);
        response.setWorkshopId(updated.getWorkshop() != null ? updated.getWorkshop().getWorkshopId() : null);
        return response;
    }

    public void deleteCarpenter(Long id) {
        if (!carpenterRepository.existsById(id)) {
            throw new EntityNotFoundException("Carpintero no encontrado con ID: " + id);
        }
        carpenterRepository.deleteById(id);
    }

}

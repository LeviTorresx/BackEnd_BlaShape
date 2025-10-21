package com.blashape.backend_blashape.services;
import com.blashape.backend_blashape.DTOs.CarpenterDTO;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.mapper.CarpenterMapper;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarpenterService {
    private final CarpenterRepository carpenterRepository;
    private final CarpenterMapper carpenterMapper;

    public CarpenterDTO getCarpenterById(Long id) {
        Carpenter carpenter = carpenterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado con ID: " + id));

        return carpenterMapper.toDTO(carpenter);
    }

    public List<CarpenterDTO> getAllCarpenters() {
        return carpenterRepository.findAll()
                .stream()
                .map(carpenterMapper::toDTO)
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

        return  carpenterMapper.toDTO(updated);
    }

    public void deleteCarpenter(Long id) {
        if (!carpenterRepository.existsById(id)) {
            throw new EntityNotFoundException("Carpintero no encontrado con ID: " + id);
        }
        carpenterRepository.deleteById(id);
    }

}

package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.WorkshopDTO;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.entitys.Workshop;
import com.blashape.backend_blashape.mapper.WorkshopMapper;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.repositories.WorkshopRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkshopService {

    private final WorkshopRepository workshopRepository;
    private final CarpenterRepository carpenterRepository;
    private final WorkshopMapper workshopMapper;

    public WorkshopDTO createWorkshop(WorkshopDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del taller es obligatorio");
        }
        if (dto.getAddress() == null || dto.getAddress().isBlank()) {
            throw new IllegalArgumentException("La dirección del taller es obligatoria");
        }
        if (dto.getPhone() == null || dto.getPhone().isBlank()) {
            throw new IllegalArgumentException("El teléfono del taller es obligatorio");
        }
        if (dto.getCarpenterId() == null) {
            throw new IllegalArgumentException("Debe indicar el ID del carpintero que crea el taller");
        }

        Workshop workshop = workshopMapper.toEntity(dto);

        if (dto.getCarpenterId() != null) {
            Carpenter carpenter = carpenterRepository.findById(dto.getCarpenterId())
                    .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado"));
            workshop.setCarpenter(carpenter);
        }

        Workshop saved = workshopRepository.save(workshop);
        return workshopMapper.toDto(saved);
    }

    public WorkshopDTO getWorkshop(Long id) {
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Taller no encontrado con ID: " + id));
        return workshopMapper.toDto(workshop);
    }

    public List<WorkshopDTO> getAllWorkshops() {
        return workshopRepository.findAll()
                .stream()
                .map(workshopMapper::toDto)
                .toList();
    }

    public WorkshopDTO getWorkshopByCarpenterId(Long carpenterId) {
        Carpenter carpenter = carpenterRepository.findById(carpenterId)
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado con ID: " + carpenterId));

        Workshop workshop = carpenter.getWorkshop();
        if (workshop == null) {
            throw new EntityNotFoundException("El carpintero con ID " + carpenterId + " no tiene taller asignado");
        }
        return workshopMapper.toDto(workshop);
    }

    public WorkshopDTO updateWorkshop(Long id, WorkshopDTO dto) {
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Taller no encontrado con ID: " + id));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            workshop.setName(dto.getName());
        }
        if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
            workshop.setAddress(dto.getAddress());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            workshop.setPhone(dto.getPhone());
        }
        if (dto.getNit() != null && !dto.getNit().isBlank()) {
            workshop.setNit(dto.getNit());
        }

        Workshop updated = workshopRepository.save(workshop);
        return workshopMapper.toDto(updated);
    }

    public void deleteWorkshop(Long id) {
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Taller no encontrado con ID: " + id));

        if (workshop.getCarpenter() != null) {
            Carpenter carpenter = workshop.getCarpenter();
            carpenter.setWorkshop(null);
            carpenterRepository.save(carpenter);
        }

        workshopRepository.delete(workshop);
    }
}


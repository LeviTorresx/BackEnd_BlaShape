package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.WorkshopDTO;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.entitys.Workshop;
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

        Carpenter carpenter = carpenterRepository.findById(dto.getCarpenterId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Carpintero no encontrado con ID: " + dto.getCarpenterId()
                ));

        if (carpenter.getWorkshop() != null) {
            throw new IllegalStateException("El carpintero ya tiene un taller asignado");
        }

        Workshop workshop = new Workshop();
        workshop.setName(dto.getName());
        workshop.setAddress(dto.getAddress());
        workshop.setPhone(dto.getPhone());
        workshop.setNit(dto.getNit());
        workshop.setCarpenter(carpenter);

        Workshop savedWorkshop = workshopRepository.save(workshop);

        carpenter.setWorkshop(savedWorkshop);
        carpenterRepository.save(carpenter);

        return toDto(savedWorkshop);
    }

    public WorkshopDTO getWorkshop(Long id) {
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Taller no encontrado con ID: " + id));
        return toDto(workshop);
    }

    public List<WorkshopDTO> getAllWorkshops() {
        return workshopRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public WorkshopDTO getWorkshopByCarpenterId(Long carpenterId) {
        Carpenter carpenter = carpenterRepository.findById(carpenterId)
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado con ID: " + carpenterId));

        Workshop workshop = carpenter.getWorkshop();
        if (workshop == null) {
            throw new EntityNotFoundException("El carpintero con ID " + carpenterId + " no tiene taller asignado");
        }
        return toDto(workshop);
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
        return toDto(updated);
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

    private WorkshopDTO toDto(Workshop workshop) {
        WorkshopDTO dto = new WorkshopDTO();
        dto.setWorkshopId(workshop.getWorkshopId());
        dto.setName(workshop.getName());
        dto.setAddress(workshop.getAddress());
        dto.setPhone(workshop.getPhone());
        dto.setNit(workshop.getNit());
        if (workshop.getCarpenter() != null) {
            dto.setCarpenterId(workshop.getCarpenter().getCarpenterId());
        }
        return dto;
    }
}


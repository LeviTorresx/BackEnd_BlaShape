package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.CarpenterDTO;
import com.blashape.backend_blashape.DTOs.WorkshopDTO;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.entitys.Workshop;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.repositories.WorkshopRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkshopService {
    private final WorkshopRepository workshopRepository;
    private final CarpenterRepository carpenterRepository;
    private final ObjectMapper objectMapper;

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
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado con ID: " + dto.getCarpenterId()));

        if (carpenter.getWorkshop() != null) {
            throw new IllegalStateException("El carpintero ya tiene un taller asignado");
        }

        Workshop workshop = objectMapper.convertValue(dto, Workshop.class);
        workshop.setCarpenter(carpenter);

        Workshop savedWorkshop = workshopRepository.save(workshop);

        carpenter.setWorkshop(savedWorkshop);
        carpenterRepository.save(carpenter);

        WorkshopDTO response = objectMapper.convertValue(savedWorkshop, WorkshopDTO.class);
        response.setCarpenterId(carpenter.getCarpenterId());
        return response;
    }

    public WorkshopDTO getWorkshop(Long id) {
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Taller no encontrado con ID: " + id));
        WorkshopDTO dto = objectMapper.convertValue(workshop, WorkshopDTO.class);
        if (workshop.getCarpenter() != null) {
            dto.setCarpenterId(workshop.getCarpenter().getCarpenterId());
        }
        return dto;
    }

    public List<WorkshopDTO> getAllWorkshops() {
        return workshopRepository.findAll()
                .stream()
                .map(workshop -> {
                    WorkshopDTO dto = objectMapper.convertValue(workshop, WorkshopDTO.class);
                    if (workshop.getCarpenter() != null) {
                        dto.setCarpenterId(workshop.getCarpenter().getCarpenterId());
                    }
                    return dto;
                })
                .toList();
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

        WorkshopDTO response = objectMapper.convertValue(updated, WorkshopDTO.class);
        if (updated.getCarpenter() != null) {
            response.setCarpenterId(updated.getCarpenter().getCarpenterId());
        }
        return response;
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

package com.blashape.backend_blashape.services;


import com.blashape.backend_blashape.DTOs.AlertDTO;
import com.blashape.backend_blashape.entitys.Alert;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.mapper.AlertMapper;
import com.blashape.backend_blashape.repositories.AlertRepository;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;
    private final CarpenterRepository carpenterRepository;

    public AlertDTO createAlert(AlertDTO dto) {

        if(dto.getMessage() == null || dto.getMessage().isBlank()){
            throw new IllegalArgumentException("Debe ingresar una mensaje");
        }

        if(dto.getDate() == null || dto.getTime() == null){
            throw new IllegalArgumentException("Debe ingresar una fecha y hora");
        }

        if(dto.getSeverity() == null){
            throw new IllegalArgumentException("La prioridad de la alerta es obligatoria");
        }

        if (dto.getCarpenterId() == null) {
            throw new IllegalArgumentException("Debe indicar el ID del carpintero que crea la alerta");
        }

        Alert alert = alertMapper.toEntity(dto);

        if (dto.getCarpenterId() != null) {
            Carpenter carpenter = carpenterRepository.findById(dto.getCarpenterId())
                    .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado"));
            alert.setCarpenter(carpenter);
        }

        try {
            Alert saved = alertRepository.save(alert);
            return alertMapper.toDTO(saved);

        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Formato de fecha u hora inválido. Usa 'yyyy-MM-dd' para la fecha y 'HH:mm:ss' para la hora.");
        }
    }

    public AlertDTO getAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new EntityNotFoundException("Alerta no encontrado con ID: " + alertId));
        return alertMapper.toDTO(alert);
    }

    public List<AlertDTO> getAlertsByCarpenterId(Long carpenterId) {
        Carpenter carpenter = carpenterRepository.findById(carpenterId)
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado con ID: " + carpenterId));

        List<Alert> alerts = alertRepository.findByCarpenter(carpenter);

        return alerts.stream()
                .map(alertMapper::toDTO)
                .toList();
    }

    public AlertDTO updateAlert(Long Id, AlertDTO dto) {
        Alert alert = alertRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("Alerta no encontrado con ID: " + Id));

        if(dto.getMessage() == null || dto.getMessage().isBlank()){
            throw new IllegalArgumentException("Debe ingresar una mensaje");
        }

        alert.setMessage(dto.getMessage());

        if (dto.getDate() != null) {
            alert.setDate(dto.getDate());
        }
        if (dto.getTime() != null) {
            alert.setTime(dto.getTime());
        }
        if (dto.getSeverity() != null) {
            alert.setSeverity(dto.getSeverity());
        }

        Alert updated =  alertRepository.save(alert);
        return alertMapper.toDTO(updated);
    }

    public void deleteAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alerta no encontrada con ID: " + id));

        alertRepository.delete(alert);
    }
}

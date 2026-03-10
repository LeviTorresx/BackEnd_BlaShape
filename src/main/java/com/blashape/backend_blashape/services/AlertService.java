package com.blashape.backend_blashape.services;


import com.blashape.backend_blashape.DTOs.AlertDTO;
import com.blashape.backend_blashape.entitys.*;
import com.blashape.backend_blashape.mapper.AlertMapper;
import com.blashape.backend_blashape.repositories.AlertRepository;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.repositories.FurnitureRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;
    private final CarpenterRepository carpenterRepository;
    private final FurnitureRepository furnitureRepository;

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

        List<Alert> alerts = alertRepository.findByCarpenterAndState(carpenter, AlertState.ACTIVE);

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

    public void generateAutomaticAlerts(){
        LocalDate today = LocalDate.now();
        LocalDate weekLater = today.plusDays(7);

        List<Furniture> furnitureList = furnitureRepository.findByEndDateAndFutureDate(today, weekLater);

        for(Furniture furniture : furnitureList){
            if(furniture.getCarpenter() == null) continue;

            long daysLeft = ChronoUnit.DAYS.between(today, furniture.getEndDate());
            Severity severity = null;
            String message = null;
            String alertMessage = "El plazo para el mueble '"+furniture.getName()+"' (ID: "+furniture.getFurnitureId().toString()+") vence";

            if (daysLeft == 7) {
                severity = Severity.MEDIUM;
                message = alertMessage+" dentro de una semana.";
            } else if (daysLeft <= 3 && daysLeft > 1) {
                severity = Severity.HIGH;
                message = alertMessage+" en menos de tres días.";
            } else if (daysLeft == 1 || daysLeft == 0) {
                severity = Severity.CRITICAL;
                message = daysLeft == 1
                        ? alertMessage+" vence mañana."
                        : alertMessage+" vence hoy.";
            }

            if (severity == null) continue;

            boolean exists = alertRepository.existsByCarpenterAndDateAndMessage(furniture.getCarpenter(), today, furniture.getName());

            if (!exists) {
                Alert alert = new Alert();
                alert.setMessage(message);
                alert.setDate(today);
                alert.setTime(LocalTime.now());
                alert.setSeverity(severity);
                alert.setCarpenter(furniture.getCarpenter());
                alertRepository.save(alert);
            }

            if (daysLeft == 0) {
                List<Alert> alertsOfFurniture = alertRepository.findByMessageContaining(
                        furniture.getName()
                );

                for (Alert a : alertsOfFurniture) {
                    a.setState(AlertState.INACTIVE);
                }

                alertRepository.saveAll(alertsOfFurniture);
            }

        }
    }
}

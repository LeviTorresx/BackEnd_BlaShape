package com.blashape.backend_blashape.mapper;

import org.mapstruct.Mapper;

import com.blashape.backend_blashape.DTOs.PaymentDTO;
import com.blashape.backend_blashape.entitys.Payment;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    Payment toEntity(PaymentDTO dto);

    PaymentDTO toDTO(Payment payment);
}

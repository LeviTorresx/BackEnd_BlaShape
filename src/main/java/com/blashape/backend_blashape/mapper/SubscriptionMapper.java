package com.blashape.backend_blashape.mapper;

import org.mapstruct.Mapper;

import com.blashape.backend_blashape.DTOs.SubscriptionDTO;
import com.blashape.backend_blashape.entitys.Subscription;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    Subscription toEntity(SubscriptionDTO dto);

    SubscriptionDTO toDTO(Subscription subscription);
}

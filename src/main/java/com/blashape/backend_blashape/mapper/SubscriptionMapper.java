package com.blashape.backend_blashape.mapper;

import org.mapstruct.Mapper;

import com.blashape.backend_blashape.DTOs.ActiveSubscription;
import com.blashape.backend_blashape.DTOs.SubscriptionDTO;
import com.blashape.backend_blashape.entitys.AppSubscription;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    AppSubscription toEntity(SubscriptionDTO dto);

    SubscriptionDTO toDTO(AppSubscription subscription);

    ActiveSubscription toActiveSubscriptionDTO(AppSubscription subscription);
}

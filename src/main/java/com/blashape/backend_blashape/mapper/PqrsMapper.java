package com.blashape.backend_blashape.mapper;

import com.blashape.backend_blashape.DTOs.PqrsDTO;
import com.blashape.backend_blashape.entitys.Pqrs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PqrsMapper {

    @Mapping(target = "customerId", source = "customer.customerId")
    @Mapping(target = "carpenterId", source = "carpenter.carpenterId")
    PqrsDTO toDTO(Pqrs pqrs);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "carpenter", ignore = true)
    Pqrs toEntity(PqrsDTO dto);
}
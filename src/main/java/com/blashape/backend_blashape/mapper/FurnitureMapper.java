package com.blashape.backend_blashape.mapper;


import com.blashape.backend_blashape.DTOs.FurnitureDTO;
import com.blashape.backend_blashape.entitys.Furniture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper (componentModel = "spring", uses= {CuttingMapper.class})
public interface FurnitureMapper {
    @Mapping(target = "carpenterId",source = "carpenter.carpenterId")
    @Mapping(target = "customerId", source = "customer.customerId")
    FurnitureDTO toDTO(Furniture furniture);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "carpenter", ignore = true)
    @Mapping(target = "cutting.furniture", ignore = true)
    Furniture toEntity(FurnitureDTO dto);
}

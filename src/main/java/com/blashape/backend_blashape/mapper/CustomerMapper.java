package com.blashape.backend_blashape.mapper;

import com.blashape.backend_blashape.DTOs.CustomerDTO;
import com.blashape.backend_blashape.entitys.Customer;
import com.blashape.backend_blashape.entitys.Furniture;
import com.blashape.backend_blashape.entitys.Carpenter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Mapping(target ="furnitureListIds", source = "furnitureList")
    @Mapping(target = "carpenterIds", source = "carpenters")
    CustomerDTO toDTO(Customer customer);

    @Mapping(target = "furnitureList", ignore = true)
    @Mapping(target = "carpenters", ignore = true)
    Customer toEntity(CustomerDTO dto);

    default List<Long> mapFurnitureListToIds(List<Furniture> furnitureList) {
        if (furnitureList == null) return null;
        return furnitureList.stream().map(Furniture::getFurnitureId).toList();
    }

    default List<Long> mapCarpentersToIds(List<Carpenter> carpenters) {
        if (carpenters == null) return null;
        return carpenters.stream().map(Carpenter::getCarpenterId).toList();
    }
}

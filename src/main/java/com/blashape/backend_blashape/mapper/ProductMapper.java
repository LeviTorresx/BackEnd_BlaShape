package com.blashape.backend_blashape.mapper;

import org.mapstruct.Mapper;

import com.blashape.backend_blashape.DTOs.ProductDTO;
import com.blashape.backend_blashape.entitys.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toEntity(ProductDTO dto);

    ProductDTO toDTO(Product product);
}

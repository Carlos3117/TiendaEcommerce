package com.example.unimagdalena.TiendaEcommerce.services.mapper;

import com.example.unimagdalena.TiendaEcommerce.dto.ProductDto.ProductResponse;
import com.example.unimagdalena.TiendaEcommerce.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    ProductResponse toResponse(Product entity);
}
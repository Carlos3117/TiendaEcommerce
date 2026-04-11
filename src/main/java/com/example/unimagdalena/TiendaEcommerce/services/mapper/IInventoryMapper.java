package com.example.unimagdalena.TiendaEcommerce.services.mapper;

import com.example.unimagdalena.TiendaEcommerce.dto.InventoryDto.InventoryResponse;
import com.example.unimagdalena.TiendaEcommerce.entities.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IInventoryMapper {

    @Mapping(target = "productId", source = "product.id")
    InventoryResponse toResponse(Inventory entity);
}
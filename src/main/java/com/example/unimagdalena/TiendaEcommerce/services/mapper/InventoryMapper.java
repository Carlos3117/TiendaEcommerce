package com.example.unimagdalena.TiendaEcommerce.services.mapper;


import com.example.unimagdalena.TiendaEcommerce.dto.InventoryDto.UpdateInventoryRequest;
import com.example.unimagdalena.TiendaEcommerce.entities.Inventory;

public class InventoryMapper {

    public static void update(Inventory inventory, UpdateInventoryRequest request) {

        inventory.setStock(request.stock());
        inventory.setMinStock(request.minStock());
    }
}
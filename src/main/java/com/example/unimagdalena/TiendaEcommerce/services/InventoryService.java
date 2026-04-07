package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.dto.InventoryDto.*;

import java.util.List;

public interface InventoryService {

    InventoryResponse updateInventory(UpdateInventoryRequest request);

    InventoryResponse getByProduct(Long productId);

    List<InventoryResponse> getLowStockProducts();
}
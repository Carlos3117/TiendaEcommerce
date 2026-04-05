package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.Inventory;

import java.util.List;

public interface InventoryService {


    Inventory updateInventory(Long productId, Integer stock, Integer minStock);


    Inventory getByProduct(Long productId);


    List<Inventory> getLowStockProducts();
}
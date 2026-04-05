package com.example.unimagdalena.TiendaEcommerce.services;


import com.example.unimagdalena.TiendaEcommerce.entities.Inventory;
import com.example.unimagdalena.TiendaEcommerce.entities.Product;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.repositories.InventoryRepository;
import com.example.unimagdalena.TiendaEcommerce.repositories.ProductRepository;
import com.example.unimagdalena.TiendaEcommerce.services.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional

public class InventoryServiceImpl implements InventoryService  {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

   

    @Override
    public Inventory updateInventory(Long productId, Integer stock, Integer minStock) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (stock == null || stock < 0) {
            throw new BusinessException("El stock no puede ser negativo");
        }

        if (minStock == null || minStock < 0) {
            throw new BusinessException("El stock mínimo no puede ser negativo");
        }

        Inventory inventory = product.getInventory();


        if (inventory == null) {
            inventory = new Inventory();
            inventory.setProduct(product);
        }

        inventory.setStock(stock);
        inventory.setMinStock(minStock);

        return inventoryRepository.save(inventory);
    }


    @Override
    @Transactional(readOnly = true)
    public Inventory getByProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Inventory inventory = product.getInventory();

        if (inventory == null) {
            throw new ResourceNotFoundException("El producto no tiene inventario");
        }

        return inventory;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Inventory> getLowStockProducts() {

        return inventoryRepository.findByStockLessThan(5);
    }
}
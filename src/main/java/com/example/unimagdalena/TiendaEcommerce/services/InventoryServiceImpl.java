package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.dto.InventoryDto.*;
import com.example.unimagdalena.TiendaEcommerce.entities.Inventory;
import com.example.unimagdalena.TiendaEcommerce.entities.Product;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.services.mapper.IInventoryMapper ;
import com.example.unimagdalena.TiendaEcommerce.services.mapper.InventoryMapper ;
import com.example.unimagdalena.TiendaEcommerce.repositories.InventoryRepository;
import com.example.unimagdalena.TiendaEcommerce.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final IInventoryMapper inventoryMapper;

    @Override
    public InventoryResponse updateInventory(UpdateInventoryRequest request) {

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Inventory inventory = product.getInventory();

        if (inventory == null) {
            inventory = new Inventory();
            inventory.setProduct(product);
            product.setInventory(inventory);
        }

        InventoryMapper.update(inventory, request);

        Inventory saved = inventoryRepository.save(inventory);

        return inventoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getByProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Inventory inventory = product.getInventory();

        if (inventory == null) {
            throw new ResourceNotFoundException("El producto no tiene inventario");
        }

        return inventoryMapper.toResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockProducts() {

        return inventoryRepository.findLowStockProducts()
                .stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }
}
package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.dto.InventoryDto.UpdateInventoryRequest;
import com.example.unimagdalena.TiendaEcommerce.dto.InventoryDto.InventoryResponse;
import com.example.unimagdalena.TiendaEcommerce.entities.*;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.services.mapper.IInventoryMapper ;
import com.example.unimagdalena.TiendaEcommerce.repositories.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock private InventoryRepository inventoryRepository;
    @Mock private ProductRepository productRepository;
    @Mock private IInventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    // Debe lanzar error si el producto no existe
    @Test
    void shouldThrowWhenProductNotFound() {

        UpdateInventoryRequest request = new UpdateInventoryRequest(1L, 10, 2);

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                inventoryService.updateInventory(request)
        );
    }

    // Debe crear inventario si no existe
    @Test
    void shouldCreateInventoryIfNotExists() {

        Product product = new Product();
        product.setId(1L);

        UpdateInventoryRequest request = new UpdateInventoryRequest(1L, 10, 2);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        when(inventoryMapper.toResponse(any()))
                .thenAnswer(inv -> {
                    Inventory i = inv.getArgument(0);
                    return new InventoryResponse(i.getProduct().getId(), i.getStock(), i.getMinStock());
                });

        InventoryResponse response = inventoryService.updateInventory(request);

        assertEquals(10, response.stock());
        assertEquals(2, response.minStock());
        assertNotNull(product.getInventory());
    }

    // Debe actualizar inventario existente
    @Test
    void shouldUpdateExistingInventory() {

        Product product = new Product();
        product.setId(1L);

        Inventory inventory = new Inventory();
        inventory.setStock(5);
        inventory.setMinStock(1);
        inventory.setProduct(product);

        product.setInventory(inventory);

        UpdateInventoryRequest request = new UpdateInventoryRequest(1L, 20, 3);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        when(inventoryMapper.toResponse(any()))
                .thenAnswer(inv -> {
                    Inventory i = inv.getArgument(0);
                    return new InventoryResponse(i.getProduct().getId(), i.getStock(), i.getMinStock());
                });

        InventoryResponse updated = inventoryService.updateInventory(request);

        assertEquals(20, updated.stock());
        assertEquals(3, updated.minStock());
    }

    // Debe lanzar error si no tiene inventario al consultar
    @Test
    void shouldThrowWhenProductHasNoInventory() {

        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(ResourceNotFoundException.class, () ->
                inventoryService.getByProduct(1L)
        );
    }

    // Debe retornar inventario correctamente
    @Test
    void shouldReturnInventory() {

        Product product = new Product();
        product.setId(1L);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setStock(10);
        inventory.setMinStock(2);

        product.setInventory(inventory);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        when(inventoryMapper.toResponse(any()))
                .thenAnswer(inv -> {
                    Inventory i = inv.getArgument(0);
                    return new InventoryResponse(i.getProduct().getId(), i.getStock(), i.getMinStock());
                });

        InventoryResponse result = inventoryService.getByProduct(1L);

        assertEquals(10, result.stock());
        assertEquals(2, result.minStock());
    }
}
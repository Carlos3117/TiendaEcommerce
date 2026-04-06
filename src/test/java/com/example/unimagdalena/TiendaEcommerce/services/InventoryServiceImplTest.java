package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.*;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
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

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    // Debe lanzar error si el producto no existe
    @Test
    void shouldThrowWhenProductNotFound() {

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                inventoryService.updateInventory(1L, 10, 2)
        );
    }

    // No debe permitir stock negativo
    @Test
    void shouldNotAllowNegativeStock() {

        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BusinessException.class, () ->
                inventoryService.updateInventory(1L, -5, 2)
        );
    }

    // No debe permitir stock mínimo negativo
    @Test
    void shouldNotAllowNegativeMinStock() {

        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BusinessException.class, () ->
                inventoryService.updateInventory(1L, 10, -1)
        );
    }

    // Debe crear inventario si no existe
    @Test
    void shouldCreateInventoryIfNotExists() {

        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Inventory inventory = inventoryService.updateInventory(1L, 10, 2);

        assertEquals(10, inventory.getStock());
        assertEquals(2, inventory.getMinStock());
        assertNotNull(product.getInventory());
    }

    //  Debe actualizar inventario existente
    @Test
    void shouldUpdateExistingInventory() {

        Product product = new Product();
        product.setId(1L);

        Inventory inventory = new Inventory();
        inventory.setStock(5);
        inventory.setMinStock(1);

        product.setInventory(inventory);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Inventory updated = inventoryService.updateInventory(1L, 20, 3);

        assertEquals(20, updated.getStock());
        assertEquals(3, updated.getMinStock());
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
        product.setInventory(inventory);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Inventory result = inventoryService.getByProduct(1L);

        assertEquals(inventory, result);
    }
}
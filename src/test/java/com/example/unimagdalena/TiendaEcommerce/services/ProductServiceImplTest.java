package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.*;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ConflictException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.repositories.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    // SKU obligatorio
    @Test
    void shouldNotCreateProductWithoutSku() {

        Product product = new Product();
        product.setPrice(BigDecimal.TEN);

        assertThrows(BusinessException.class, () ->
                productService.createProduct(product)
        );
    }

    // SKU duplicado
    @Test
    void shouldNotAllowDuplicateSku() {

        Product product = new Product();
        product.setSku("ABC123");
        product.setPrice(BigDecimal.TEN);

        when(productRepository.findBySku("ABC123"))
                .thenReturn(Optional.of(new Product()));

        assertThrows(ConflictException.class, () ->
                productService.createProduct(product)
        );
    }

    // Precio inválido
    @Test
    void shouldNotAllowInvalidPrice() {

        Product product = new Product();
        product.setSku("ABC123");
        product.setPrice(BigDecimal.ZERO);

        assertThrows(BusinessException.class, () ->
                productService.createProduct(product)
        );
    }

    // Categoría inexistente
    @Test
    void shouldThrowWhenCategoryNotFound() {

        Product product = new Product();
        product.setSku("ABC123");
        product.setPrice(BigDecimal.TEN);

        Category category = new Category();
        category.setId(1L);

        product.setCategory(category);

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                productService.createProduct(product)
        );
    }

    // Crear producto correctamente
    @Test
    void shouldCreateProductSuccessfully() {

        Product product = new Product();
        product.setSku("ABC123");
        product.setPrice(BigDecimal.TEN);

        Category category = new Category();
        category.setId(1L);

        product.setCategory(category);

        when(productRepository.findBySku("ABC123")).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product saved = productService.createProduct(product);

        assertEquals("ABC123", saved.getSku());
        assertEquals(category, saved.getCategory());
    }

    // Producto no encontrado
    @Test
    void shouldThrowWhenProductNotFound() {

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                productService.getProductById(1L)
        );
    }

    // Cambiar estado del producto
    @Test
    void shouldChangeProductStatus() {

        Product product = new Product();
        product.setId(1L);
        product.setActive(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product updated = productService.changeProductStatus(1L, false);

        assertFalse(updated.getActive());
    }
}
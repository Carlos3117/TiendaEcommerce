package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.dto.ProductDto.*;
import com.example.unimagdalena.TiendaEcommerce.entities.*;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ConflictException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.services.mapper.IProductMapper;
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
    @Mock private IProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    // SKU duplicado
    @Test
    void shouldNotAllowDuplicateSku() {

        CreateProductRequest request = new CreateProductRequest(
                "Producto",
                "ABC123",
                BigDecimal.TEN,
                1L
        );

        when(productRepository.findBySku("ABC123"))
                .thenReturn(Optional.of(new Product()));

        assertThrows(ConflictException.class, () ->
                productService.createProduct(request)
        );
    }

    // Categoría inexistente
    @Test
    void shouldThrowWhenCategoryNotFound() {

        CreateProductRequest request = new CreateProductRequest(
                "Producto",
                "ABC123",
                BigDecimal.TEN,
                1L
        );

        when(productRepository.findBySku("ABC123")).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                productService.createProduct(request)
        );
    }

    // Crear producto correctamente
    @Test
    void shouldCreateProductSuccessfully() {

        Category category = new Category();
        category.setId(1L);

        CreateProductRequest request = new CreateProductRequest(
                "Producto",
                "ABC123",
                BigDecimal.TEN,
                1L
        );

        when(productRepository.findBySku("ABC123")).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        when(productMapper.toResponse(any()))
                .thenAnswer(inv -> {
                    Product p = inv.getArgument(0);
                    return new ProductResponse(
                            p.getId(),
                            p.getName(),
                            p.getSku(),
                            p.getPrice(),
                            p.getActive(),
                            p.getCategory().getId()
                    );
                });

        ProductResponse response = productService.createProduct(request);

        assertEquals("ABC123", response.sku());
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

        when(productMapper.toResponse(any()))
                .thenAnswer(inv -> {
                    Product p = inv.getArgument(0);
                    return new ProductResponse(
                            p.getId(),
                            p.getName(),
                            p.getSku(),
                            p.getPrice(),
                            p.getActive(),
                            p.getCategory() != null ? p.getCategory().getId() : null
                    );
                });

        ProductResponse updated = productService.changeProductStatus(1L, false);

        assertFalse(updated.active());
    }
}
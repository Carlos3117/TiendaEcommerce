package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.dto.ProductDto.*;
import com.example.unimagdalena.TiendaEcommerce.entities.Category;
import com.example.unimagdalena.TiendaEcommerce.entities.Product;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ConflictException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.services.mapper.IProductMapper;
import com.example.unimagdalena.TiendaEcommerce.services.mapper.ProductMapper;
import com.example.unimagdalena.TiendaEcommerce.repositories.CategoryRepository;
import com.example.unimagdalena.TiendaEcommerce.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final IProductMapper productMapper;

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {

        productRepository.findBySku(request.sku())
                .ifPresent(p -> {
                    throw new ConflictException("El SKU ya existe");
                });

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        Product product = ProductMapper.toEntity(request, category);

        Product saved = productRepository.save(product);

        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (request.sku() != null) {
            productRepository.findBySku(request.sku())
                    .ifPresent(p -> {
                        if (!p.getId().equals(id)) {
                            throw new ConflictException("El SKU ya existe");
                        }
                    });
        }

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        }

        ProductMapper.patch(existing, request, category);

        Product updated = productRepository.save(existing);

        return productMapper.toResponse(updated);
    }

    @Override
    public ProductResponse changeProductStatus(Long id, Boolean active) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (Boolean.FALSE.equals(active)) {

            boolean hasActiveOrders = product.getOrderItems() != null &&
                    product.getOrderItems().stream()
                            .anyMatch(item ->
                                    item.getOrder().getStatus() == OrderStatus.CREATED ||
                                            item.getOrder().getStatus() == OrderStatus.PAID
                            );

            if (hasActiveOrders) {
                throw new BusinessException("No se puede desactivar un producto con pedidos activos");
            }
        }

        product.setActive(active);

        Product saved = productRepository.save(product);

        return productMapper.toResponse(saved);
    }
}
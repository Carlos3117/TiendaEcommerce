package com.example.unimagdalena.TiendaEcommerce.services;


import com.example.unimagdalena.TiendaEcommerce.entities.Category;
import com.example.unimagdalena.TiendaEcommerce.entities.Product;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ConflictException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.repositories.CategoryRepository;
import com.example.unimagdalena.TiendaEcommerce.repositories.ProductRepository;
import com.example.unimagdalena.TiendaEcommerce.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional

public class ProductServiceImpl  implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Product createProduct(Product product) {


        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio debe ser mayor que cero");
        }


        if (product.getSku() == null || product.getSku().isBlank()) {
            throw new BusinessException("El SKU es obligatorio");
        }


        productRepository.findBySku(product.getSku())
                .ifPresent(p -> {
                    throw new ConflictException("El SKU ya existe");
                });


        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new BusinessException("La categoría es obligatoria");
        }

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        product.setCategory(category);


        if (product.getActive() == null) {
            product.setActive(true);
        }

        return productRepository.save(product);
    }


    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }


    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    @Override
    public Product updateProduct(Long id, Product updatedProduct) {

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));


        if (updatedProduct.getName() != null) {
            existing.setName(updatedProduct.getName());
        }

        if (updatedProduct.getPrice() != null) {
            if (updatedProduct.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("El precio debe ser mayor que cero");
            }
            existing.setPrice(updatedProduct.getPrice());
        }


        if (updatedProduct.getSku() != null && !updatedProduct.getSku().isBlank()) {

            productRepository.findBySku(updatedProduct.getSku())
                    .ifPresent(p -> {
                        if (!p.getId().equals(id)) {
                            throw new ConflictException("El SKU ya existe");
                        }
                    });

            existing.setSku(updatedProduct.getSku());
        }


        if (updatedProduct.getCategory() != null && updatedProduct.getCategory().getId() != null) {

            Category category = categoryRepository.findById(updatedProduct.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

            existing.setCategory(category);
        }

        return productRepository.save(existing);
    }


    @Override
    public Product changeProductStatus(Long id, Boolean active) {

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
        return productRepository.save(product);
    }
}
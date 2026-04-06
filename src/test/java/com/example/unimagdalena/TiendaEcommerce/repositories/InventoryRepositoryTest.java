package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.Category;
import com.example.unimagdalena.TiendaEcommerce.entities.Inventory;
import com.example.unimagdalena.TiendaEcommerce.entities.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@Transactional
@Testcontainers
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
class InventoryRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldFindInventoriesWithLowStock() {

        Category category = categoryRepository.save(
                new Category(null, "Tech", null)
        );

        Product product1 = productRepository.save(
                new Product(null, "Mouse", "SKU1",
                        new BigDecimal("20"), true, category, null, null)
        );

        Product product2 = productRepository.save(
                new Product(null, "Keyboard", "SKU2",
                        new BigDecimal("50"), true, category, null, null)
        );

        inventoryRepository.save(
                new Inventory(null, 2, 5, product1)
        );

        inventoryRepository.save(
                new Inventory(null, 10, 5, product2)
        );

        List<Inventory> result = inventoryRepository.findByStockLessThan(5);

        assertEquals(1, result.size());
        assertEquals("Mouse", result.get(0).getProduct().getName());
    }


    @Test
    void shouldFindProductsWithInsufficientStock() {

        Category category = categoryRepository.save(
                new Category(null, "Accessories", null)
        );

        Product product1 = productRepository.save(
                new Product(null, "Headphones", "SKU3",
                        new BigDecimal("100"), true, category, null, null)
        );

        Product product2 = productRepository.save(
                new Product(null, "Charger", "SKU4",
                        new BigDecimal("30"), true, category, null, null)
        );

        inventoryRepository.save(
                new Inventory(null, 1, 5, product1)
        );

        inventoryRepository.save(
                new Inventory(null, 10, 5, product2)
        );

        List<Product> result =
                inventoryRepository.findProductsWithInsufficientStock();

        assertEquals(1, result.size());
        assertEquals("Headphones", result.get(0).getName());
    }
    @Test
    void shouldFindLowStockProducts() {

        Category category = categoryRepository.save(
                new Category(null, "Tech", null)
        );

        Product product1 = productRepository.save(
                new Product(null, "Mouse", "SKU1",
                        new BigDecimal("50"), true, category, null, null)
        );

        Product product2 = productRepository.save(
                new Product(null, "Keyboard", "SKU2",
                        new BigDecimal("80"), true, category, null, null)
        );

        inventoryRepository.save(
                new Inventory(null, 5, 10, product1)
        );

        inventoryRepository.save(
                new Inventory(null, 20, 10, product2)
        );

        inventoryRepository.flush();

        List<Inventory> result = inventoryRepository.findLowStockProducts();

        assertEquals(1, result.size());
        assertEquals(product1.getId(), result.get(0).getProduct().getId());
    }
}
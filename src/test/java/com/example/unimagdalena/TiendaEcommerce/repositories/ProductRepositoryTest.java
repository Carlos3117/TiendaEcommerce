package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.Category;
import com.example.unimagdalena.TiendaEcommerce.entities.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@Transactional
@Testcontainers
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
class ProductRepositoryTest {

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
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldFindProductBySku() {

        Category category = categoryRepository.save(
                new Category(null, "Electronics", null)
        );

        productRepository.save(
                new Product(
                        null,
                        "Laptop",
                        "SKU123",
                        new BigDecimal("1500"),
                        true,
                        category,
                        null,
                        null
                )
        );

        Optional<Product> found = productRepository.findBySku("SKU123");

        assertTrue(found.isPresent());
        assertEquals("Laptop", found.get().getName());
    }

    @Test
    void shouldFindActiveProductsByCategory() {

        Category category = categoryRepository.save(
                new Category(null, "Books", null)
        );

        productRepository.save(
                new Product(
                        null,
                        "Book 1",
                        "SKU1",
                        new BigDecimal("50"),
                        true,
                        category,
                        null,
                        null
                )
        );

        productRepository.save(
                new Product(
                        null,
                        "Book 2",
                        "SKU2",
                        new BigDecimal("60"),
                        false,
                        category,
                        null,
                        null
                )
        );

        List<Product> products =
                productRepository.findByCategoryAndActiveTrue(category);

        assertEquals(1, products.size());
        assertEquals("Book 1", products.get(0).getName());
    }
}
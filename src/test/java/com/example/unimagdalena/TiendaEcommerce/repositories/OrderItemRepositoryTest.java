package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.*;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
import com.example.unimagdalena.TiendaEcommerce.enums.CustomerStatus;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@Testcontainers
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
class OrderItemRepositoryTest {

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
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void shouldFindBestSellingProductsForRange() {

        Category category = categoryRepository.save(
                new Category(null, "Tech", null)
        );

        Product product1 = productRepository.save(
                new Product(null, "Laptop", "SKU1",
                        new BigDecimal("1000"), true, category, null, null)
        );

        Product product2 = productRepository.save(
                new Product(null, "Mouse", "SKU2",
                        new BigDecimal("50"), true, category, null, null)
        );

        Customer customer = customerRepository.save(
                new Customer(null, "Juan", "Perez", "juan@test.com",
                        "123", CustomerStatus.ACTIVE, null, null)
        );

        Address address = addressRepository.save(
                new Address(null, "Street", "City", customer)
        );

        Order order = orderRepository.save(
                new Order(null, customer, address, OrderStatus.PAID,
                        new BigDecimal("1100"),
                        LocalDateTime.now(),
                        null, null)
        );

        orderItemRepository.save(
                new OrderItem(null, new BigDecimal("1000"),
                        1, new BigDecimal("1000"), order, product1)
        );

        orderItemRepository.save(
                new OrderItem(null, new BigDecimal("50"),
                        5, new BigDecimal("250"), order, product2)
        );

        List<Object[]> result =
                orderItemRepository.findBestSellingProductsForRange(
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(1)
                );

        assertEquals(2, result.size());
        assertEquals(product2.getId(), ((Product) result.get(0)[0]).getId());
    }

    @Test
    void shouldFindTopCategorySellers() {

        Category category1 = categoryRepository.save(
                new Category(null, "Electronics", null)
        );

        Category category2 = categoryRepository.save(
                new Category(null, "Books", null)
        );

        Product product1 = productRepository.save(
                new Product(null, "Laptop", "SKU3",
                        new BigDecimal("1000"), true, category1, null, null)
        );

        Product product2 = productRepository.save(
                new Product(null, "Book", "SKU4",
                        new BigDecimal("30"), true, category2, null, null)
        );

        Customer customer = customerRepository.save(
                new Customer(null, "Ana", "Lopez", "ana@test.com",
                        "123", CustomerStatus.ACTIVE, null, null)
        );

        Address address = addressRepository.save(
                new Address(null, "Street", "City", customer)
        );

        Order order = orderRepository.save(
                new Order(null, customer, address, OrderStatus.PAID,
                        new BigDecimal("1030"),
                        LocalDateTime.now(),
                        null, null)
        );

        orderItemRepository.save(
                new OrderItem(null, new BigDecimal("1000"),
                        2, new BigDecimal("2000"), order, product1)
        );

        orderItemRepository.save(
                new OrderItem(null, new BigDecimal("30"),
                        1, new BigDecimal("30"), order, product2)
        );

        List<Object[]> result =
                orderItemRepository.findTopCategorySellers();

        assertEquals(2, result.size());
        assertEquals(category1.getId(), ((Category) result.get(0)[0]).getId());
    }
}
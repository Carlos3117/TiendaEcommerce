package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.*;
import com.example.unimagdalena.TiendaEcommerce.enums.CustomerStatus;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
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
class OrderStatusHistoryRepositoryTest {

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
    private OrderStatusHistoryRepository historyRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void shouldFindHistoryByOrderOrderedByDate() {

        Customer customer = customerRepository.save(
                new Customer(null, "Luis", "Martinez", "luis@test.com",
                        "123", CustomerStatus.ACTIVE, null, null)
        );

        Address address = addressRepository.save(
                new Address(null, "Street", "City", customer)
        );

        Order order = orderRepository.save(
                new Order(null, customer, address, OrderStatus.CREATED,
                        new BigDecimal("100"),
                        LocalDateTime.now(),
                        null, null)
        );

        historyRepository.save(
                new OrderStatusHistory(null, OrderStatus.CREATED,
                        LocalDateTime.now().minusHours(2), order)
        );

        historyRepository.save(
                new OrderStatusHistory(null, OrderStatus.PAID,
                        LocalDateTime.now().minusHours(1), order)
        );

        historyRepository.save(
                new OrderStatusHistory(null, OrderStatus.SHIPPED,
                        LocalDateTime.now(), order)
        );

        List<OrderStatusHistory> result =
                historyRepository.findHistorialByOrder(order);

        assertEquals(3, result.size());
        assertEquals(OrderStatus.CREATED, result.get(0).getStatus());
        assertEquals(OrderStatus.PAID, result.get(1).getStatus());
        assertEquals(OrderStatus.SHIPPED, result.get(2).getStatus());
    }
    @Test
    void shouldReturnOnlyHistoryOfGivenOrder() {

        Customer customer = customerRepository.save(
                new Customer(null, "Multi", "Test", "multi@test.com",
                        "123", CustomerStatus.ACTIVE, null, null)
        );

        Address address = addressRepository.save(
                new Address(null, "Street", "City", customer)
        );

        Order order1 = orderRepository.save(
                new Order(null, customer, address, OrderStatus.CREATED,
                        new BigDecimal("100"),
                        LocalDateTime.now(),
                        null, null)
        );

        Order order2 = orderRepository.save(
                new Order(null, customer, address, OrderStatus.CREATED,
                        new BigDecimal("200"),
                        LocalDateTime.now(),
                        null, null)
        );

        historyRepository.save(
                new OrderStatusHistory(null, OrderStatus.CREATED,
                        LocalDateTime.now(), order1)
        );

        historyRepository.save(
                new OrderStatusHistory(null, OrderStatus.PAID,
                        LocalDateTime.now(), order2)
        );

        List<OrderStatusHistory> result =
                historyRepository.findHistorialByOrder(order1);

        assertEquals(1, result.size());
        assertEquals(order1.getId(), result.get(0).getOrder().getId());
    }
    @Test
    void shouldReturnEmptyListWhenOrderHasNoHistory() {

        Customer customer = customerRepository.save(
                new Customer(null, "Test", "User", "test@test.com",
                        "123", CustomerStatus.ACTIVE, null, null)
        );

        Address address = addressRepository.save(
                new Address(null, "Street", "City", customer)
        );

        Order order = orderRepository.save(
                new Order(null, customer, address, OrderStatus.CREATED,
                        new BigDecimal("100"),
                        LocalDateTime.now(),
                        null, null)
        );

        List<OrderStatusHistory> result =
                historyRepository.findHistorialByOrder(order);

        assertTrue(result.isEmpty());
    }
}
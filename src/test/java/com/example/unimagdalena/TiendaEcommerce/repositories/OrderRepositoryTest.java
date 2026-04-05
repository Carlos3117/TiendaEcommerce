package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.*;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
class OrderRepositoryTest {

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
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void shouldFindOrdersWithFilters() {

        Customer customer = customerRepository.save(
                new Customer(null, "Juan", "Perez", "juan@test.com",
                        "123456", null, null, null)
        );

        Address address = addressRepository.save(
                new Address(null, "Street 1", "City", customer)
        );

        Order order1 = orderRepository.save(
                new Order(null, customer, address, OrderStatus.CREATED,
                        new BigDecimal("100"),
                        LocalDateTime.now().minusDays(2),
                        null, null)
        );

        Order order2 = orderRepository.save(
                new Order(null, customer, address, OrderStatus.PAID,
                        new BigDecimal("300"),
                        LocalDateTime.now(),
                        null, null)
        );

        List<Order> result = orderRepository.findOrdersWithFilters(
                customer,
                OrderStatus.PAID,
                null,
                null,
                new BigDecimal("200"),
                null
        );

        assertEquals(1, result.size());
        assertEquals(OrderStatus.PAID, result.get(0).getStatus());
    }

    @Test
    void shouldGetMonthlyIncome() {

        Customer customer = customerRepository.save(
                new Customer(null, "Ana", "Lopez", "ana@test.com",
                        "123456", null, null, null)
        );

        Address address = addressRepository.save(
                new Address(null, "Street 2", "City", customer)
        );

        orderRepository.save(
                new Order(null, customer, address, OrderStatus.PAID,
                        new BigDecimal("200"),
                        LocalDateTime.now(),
                        null, null)
        );

        orderRepository.save(
                new Order(null, customer, address, OrderStatus.PAID,
                        new BigDecimal("300"),
                        LocalDateTime.now(),
                        null, null)
        );

        List<Object[]> result = orderRepository.getMonthlyIncome();

        assertFalse(result.isEmpty());
        assertEquals(new BigDecimal("500"), result.get(0)[2]);
    }

    @Test
    void shouldFindTopSpendingCustomers() {

        Customer customer1 = customerRepository.save(
                new Customer(null, "Carlos", "Perez", "carlos@test.com",
                        "123456", null, null, null)
        );

        Customer customer2 = customerRepository.save(
                new Customer(null, "Maria", "Gomez", "maria@test.com",
                        "123456", null, null, null)
        );

        Address address1 = addressRepository.save(
                new Address(null, "Street 3", "City", customer1)
        );

        Address address2 = addressRepository.save(
                new Address(null, "Street 4", "City", customer2)
        );

        orderRepository.save(
                new Order(null, customer1, address1, OrderStatus.PAID,
                        new BigDecimal("500"),
                        LocalDateTime.now(),
                        null, null)
        );

        orderRepository.save(
                new Order(null, customer2, address2, OrderStatus.PAID,
                        new BigDecimal("100"),
                        LocalDateTime.now(),
                        null, null)
        );

        List<Object[]> result = orderRepository.findTopSpendingCustomers();

        assertEquals(2, result.size());
        assertEquals(customer1.getId(), ((Customer) result.get(0)[0]).getId());
    }
}
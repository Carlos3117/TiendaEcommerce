package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.*;
import com.example.unimagdalena.TiendaEcommerce.entities.*;
import com.example.unimagdalena.TiendaEcommerce.enums.CustomerStatus;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.repositories.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private OrderStatusHistoryRepository historyRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    // No debe crear pedido sin items
    @Test
    void shouldNotCreateOrderWithoutItems() {

        CreateOrderRequest request = new CreateOrderRequest(1L, 1L, List.of());

        assertThrows(BusinessException.class, () ->
                orderService.createOrder(request)
        );
    }

    // Cliente no existe
    @Test
    void shouldThrowWhenCustomerNotFound() {

        CreateOrderItemRequest item = new CreateOrderItemRequest(1L, 1);
        CreateOrderRequest request = new CreateOrderRequest(1L, 1L, List.of(item));

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.createOrder(request)
        );
    }

    // Cliente inactivo
    @Test
    void shouldThrowWhenCustomerIsInactive() {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setStatus(CustomerStatus.INACTIVE);

        CreateOrderItemRequest item = new CreateOrderItemRequest(1L, 1);
        CreateOrderRequest request = new CreateOrderRequest(1L, 1L, List.of(item));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(BusinessException.class, () ->
                orderService.createOrder(request)
        );
    }

    // Dirección no pertenece
    @Test
    void shouldThrowWhenAddressDoesNotBelongToCustomer() {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setStatus(CustomerStatus.ACTIVE);

        Customer other = new Customer();
        other.setId(2L);

        Address address = new Address();
        address.setCustomer(other);

        CreateOrderItemRequest item = new CreateOrderItemRequest(1L, 1);
        CreateOrderRequest request = new CreateOrderRequest(1L, 1L, List.of(item));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        assertThrows(BusinessException.class, () ->
                orderService.createOrder(request)
        );
    }

    // Producto no existe
    @Test
    void shouldThrowWhenProductNotFound() {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setStatus(CustomerStatus.ACTIVE);

        Address address = new Address();
        address.setCustomer(customer);

        CreateOrderItemRequest item = new CreateOrderItemRequest(1L, 1);
        CreateOrderRequest request = new CreateOrderRequest(1L, 1L, List.of(item));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.createOrder(request)
        );
    }

    // Producto inactivo
    @Test
    void shouldThrowWhenProductIsInactive() {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setStatus(CustomerStatus.ACTIVE);

        Address address = new Address();
        address.setCustomer(customer);

        Product product = new Product();
        product.setId(1L);
        product.setActive(false);

        CreateOrderItemRequest item = new CreateOrderItemRequest(1L, 1);
        CreateOrderRequest request = new CreateOrderRequest(1L, 1L, List.of(item));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BusinessException.class, () ->
                orderService.createOrder(request)
        );
    }

    // Flujo correcto
    @Test
    void shouldCreateOrderCorrectly() {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setStatus(CustomerStatus.ACTIVE);

        Address address = new Address();
        address.setCustomer(customer);

        Product product = new Product();
        product.setId(1L);
        product.setActive(true);
        product.setPrice(BigDecimal.valueOf(100));

        CreateOrderItemRequest item = new CreateOrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest(1L, 1L, List.of(item));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.createOrder(request);

        assertEquals(0, BigDecimal.valueOf(200).compareTo(response.total()));
        assertEquals("CREATED", response.status());
    }

    // Stock insuficiente
    @Test
    void shouldRejectPaymentWhenStockIsInsufficient() {

        Product product = new Product();

        Inventory inventory = new Inventory();
        inventory.setStock(1);
        product.setInventory(inventory);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(5);

        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setItems(List.of(item));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () ->
                orderService.payOrder(1L)
        );
    }
}
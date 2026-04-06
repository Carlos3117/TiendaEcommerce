package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.*;
import com.example.unimagdalena.TiendaEcommerce.enums.CustomerStatus;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ValidationException;
import com.example.unimagdalena.TiendaEcommerce.repositories.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    // No debe crear un pedido sin ítems
    @Test
    void shouldNotCreateOrderWithoutItems() {
        assertThrows(BusinessException.class, () ->
                orderService.createOrder(1L, 1L, List.of())
        );
    }

    // No debe permitir cantidades inválidas o negativas
    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void shouldNotCreateOrderWithInvalidQuantity(int quantity) {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setStatus(CustomerStatus.ACTIVE);

        Address address = new Address();
        address.setCustomer(customer);

        Product product = new Product();
        product.setId(1L);
        product.setActive(true);
        product.setPrice(BigDecimal.TEN);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(quantity);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        assertThrows(ValidationException.class, () ->
                orderService.createOrder(1L, 1L, List.of(item))
        );
    }

    // Debe lanzar error si el cliente no existe
    @Test
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.createOrder(1L, 1L, List.of(new OrderItem()))
        );
    }

    // Debe lanzar error si el cliente está inactivo
    @Test
    void shouldThrowWhenCustomerIsInactive() {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setStatus(CustomerStatus.INACTIVE);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(BusinessException.class, () ->
                orderService.createOrder(1L, 1L, List.of(new OrderItem()))
        );
    }

    // La dirección debe pertenecer al cliente
    @Test
    void shouldThrowWhenAddressDoesNotBelongToCustomer() {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setStatus(CustomerStatus.ACTIVE);

        Customer anotherCustomer = new Customer();
        anotherCustomer.setId(2L);

        Address address = new Address();
        address.setCustomer(anotherCustomer);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        assertThrows(BusinessException.class, () ->
                orderService.createOrder(1L, 1L, List.of(new OrderItem()))
        );
    }

    // Debe lanzar error si el producto no existe
    @Test
    void shouldThrowWhenProductNotFound() {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setStatus(CustomerStatus.ACTIVE);

        Address address = new Address();
        address.setCustomer(customer);

        Product product = new Product();
        product.setId(1L);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(1);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.createOrder(1L, 1L, List.of(item))
        );
    }

    // No debe permitir productos inactivos
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

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(1);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BusinessException.class, () ->
                orderService.createOrder(1L, 1L, List.of(item))
        );
    }

    // Debe calcular correctamente el total, asignar precio y estado inicial
    @Test
    void shouldCreateOrderWithCorrectTotalAndStatus() {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setStatus(CustomerStatus.ACTIVE);

        Address address = new Address();
        address.setCustomer(customer);

        Product product = new Product();
        product.setId(1L);
        product.setActive(true);
        product.setPrice(BigDecimal.valueOf(100));

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order order = orderService.createOrder(1L, 1L, List.of(item));

        assertEquals(0, BigDecimal.valueOf(200).compareTo(order.getTotal()));
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals(product.getPrice(), order.getItems().get(0).getUnitPrice());
    }

    // No debe permitir pagar si no hay stock suficiente
    @Test
    void shouldRejectPaymentWhenStockIsInsufficient() {

        Product product = new Product();
        product.setId(1L);
        product.setActive(true);

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

    // Debe descontar el stock al pagar
    @Test
    void shouldDecreaseStockWhenPayingOrder() {

        Product product = new Product();
        product.setActive(true);
        product.setPrice(BigDecimal.TEN);

        Inventory inventory = new Inventory();
        inventory.setStock(10);
        product.setInventory(inventory);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);

        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setItems(List.of(item));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.payOrder(1L);

        assertEquals(8, inventory.getStock());
        verify(inventoryRepository).save(inventory);
    }

    // Debe revertir el stock al cancelar un pedido pagado
    @Test
    void shouldRestoreStockWhenCancellingPaidOrder() {

        Product product = new Product();

        Inventory inventory = new Inventory();
        inventory.setStock(5);
        product.setInventory(inventory);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(3);

        Order order = new Order();
        order.setStatus(OrderStatus.PAID);
        order.setItems(List.of(item));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L);

        assertEquals(8, inventory.getStock());
        verify(inventoryRepository).save(inventory);
    }

    // No debe cancelar un pedido entregado
    @Test
    void shouldNotCancelDeliveredOrder() {

        Order order = new Order();
        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () ->
                orderService.cancelOrder(1L)
        );
    }

    // No debe enviar un pedido si no está pagado
    @Test
    void shouldNotShipOrderIfNotPaid() {

        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () ->
                orderService.shipOrder(1L)
        );
    }
}
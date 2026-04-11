package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.*;
import com.example.unimagdalena.TiendaEcommerce.entities.*;
import com.example.unimagdalena.TiendaEcommerce.enums.CustomerStatus;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
import com.example.unimagdalena.TiendaEcommerce.exceptions.*;
import com.example.unimagdalena.TiendaEcommerce.services.mapper.OrderMapper;
import com.example.unimagdalena.TiendaEcommerce.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessException("El cliente no está activo");
        }

        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessException("La dirección no pertenece al cliente");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setAddress(address);
        order.setStatus(OrderStatus.CREATED);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CreateOrderItemRequest item : request.items()) {

            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            if (!product.getActive()) {
                throw new BusinessException("Producto inactivo: " + product.getName());
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.quantity()));

            OrderItem newItem = new OrderItem();
            newItem.setOrder(order);
            newItem.setProduct(product);
            newItem.setQuantity(item.quantity());
            newItem.setUnitPrice(unitPrice);
            newItem.setSubtotal(subtotal);

            total = total.add(subtotal);
            orderItems.add(newItem);
        }

        order.setItems(orderItems);
        order.setTotal(total);

        Order saved = orderRepository.save(order);

        saveHistory(saved, OrderStatus.CREATED);

        return OrderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        return OrderMapper.toResponse(getOrderOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @Override
    public void payOrder(Long orderId) {

        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("Solo pedidos en estado CREATED pueden pagarse");
        }

        for (OrderItem item : order.getItems()) {

            Inventory inventory = item.getProduct().getInventory();

            if (inventory == null || inventory.getStock() < item.getQuantity()) {
                throw new BusinessException("Stock insuficiente");
            }
        }

        for (OrderItem item : order.getItems()) {
            Inventory inventory = item.getProduct().getInventory();
            inventory.setStock(inventory.getStock() - item.getQuantity());
            inventoryRepository.save(inventory);
        }

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        saveHistory(order, OrderStatus.PAID);
    }

    @Override
    public void shipOrder(Long orderId) {

        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("Solo pedidos pagados pueden enviarse");
        }

        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);

        saveHistory(order, OrderStatus.SHIPPED);
    }

    @Override
    public void deliverOrder(Long orderId) {

        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new BusinessException("Solo pedidos enviados pueden entregarse");
        }

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        saveHistory(order, OrderStatus.DELIVERED);
    }

    @Override
    public void cancelOrder(Long orderId) {

        Order order = getOrderOrThrow(orderId);

        switch (order.getStatus()) {

            case CREATED:
                order.setStatus(OrderStatus.CANCELLED);
                break;

            case PAID:
                for (OrderItem item : order.getItems()) {
                    Inventory inventory = item.getProduct().getInventory();
                    inventory.setStock(inventory.getStock() + item.getQuantity());
                    inventoryRepository.save(inventory);
                }
                order.setStatus(OrderStatus.CANCELLED);
                break;

            case SHIPPED:
                throw new BusinessException("No se puede cancelar un pedido que ya fue enviado");
            case DELIVERED:
                throw new BusinessException("No se puede cancelar este pedido");

            case CANCELLED:
                throw new BusinessException("El pedido ya está cancelado");
        }

        orderRepository.save(order);
        saveHistory(order, OrderStatus.CANCELLED);
    }

    private Order getOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
    }

    private void saveHistory(Order order, OrderStatus status) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(status);
        historyRepository.save(history);
    }
}
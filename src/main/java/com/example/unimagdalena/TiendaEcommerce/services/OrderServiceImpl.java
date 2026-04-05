package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.*;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.repositories.InventoryRepository;
import com.example.unimagdalena.TiendaEcommerce.repositories.OrderRepository;
import com.example.unimagdalena.TiendaEcommerce.repositories.OrderStatusHistoryRepository;
import com.example.unimagdalena.TiendaEcommerce.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional

public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderStatusHistoryRepository historyRepository;


    @Override
    public Order createOrder() {
        // Implementación real requiere DTOs (CreateOrderRequest)
        throw new UnsupportedOperationException("createOrder no implementado aún");
    }


    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return getOrderOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }


    @Override
    public void payOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("Solo pedidos en estado CREATED pueden pagarse");
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BusinessException("El pedido no tiene ítems");
        }


        for (OrderItem item : order.getItems()) {
            Inventory inventory = item.getProduct().getInventory();

            if (inventory == null) {
                throw new BusinessException("Producto sin inventario asociado");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException("Cantidad inválida en ítems");
            }

            if (inventory.getStock() < item.getQuantity()) {
                throw new BusinessException("Stock insuficiente para el producto: " +
                        item.getProduct().getName());
            }
        }


        for (OrderItem item : order.getItems()) {
            Inventory inventory = item.getProduct().getInventory();
            inventory.setStock(inventory.getStock() - item.getQuantity());
            inventoryRepository.save(inventory);
        }


        order.setStatus(OrderStatus.PAID);


        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            if (item.getUnitPrice() == null) {
                item.setUnitPrice(item.getProduct().getPrice());
            }
            if (item.getSubtotal() == null) {
                item.setSubtotal(item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())));
            }
            total = total.add(item.getSubtotal());
        }
        order.setTotal(total);

        orderRepository.save(order);
        saveHistory(order, OrderStatus.PAID);
    }


    @Override
    public void shipOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);


        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("Solo pedidos en estado PAID pueden enviarse");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("No se puede enviar un pedido cancelado");
        }

        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);

        saveHistory(order, OrderStatus.SHIPPED);
    }

    @Override
    public void deliverOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);


        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new BusinessException("Solo pedidos enviados pueden marcarse como entregados");
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
                throw new BusinessException("No se puede cancelar un pedido ya enviado");

            case DELIVERED:
                throw new BusinessException("No se puede cancelar un pedido entregado");

            case CANCELLED:
                throw new BusinessException("El pedido ya está cancelado");
        }

        orderRepository.save(order);
        saveHistory(order, OrderStatus.CANCELLED);
    }


    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
    }

    private void saveHistory(Order order, OrderStatus status) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(status);
        historyRepository.save(history);
    }
}
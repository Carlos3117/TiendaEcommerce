package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.*;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.repositories.InventoryRepository;
import com.example.unimagdalena.TiendaEcommerce.repositories.OrderRepository;
import com.example.unimagdalena.TiendaEcommerce.repositories.OrderStatusHistoryRepository;
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

        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setTotal(BigDecimal.ZERO);

        Order saved = orderRepository.save(order);

        saveHistory(saved, OrderStatus.CREATED);

        return saved;
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

            if (item.getProduct() == null) {
                throw new BusinessException("Ítem sin producto asociado");
            }

            if (!item.getProduct().getActive()) {
                throw new BusinessException("Producto inactivo: " + item.getProduct().getName());
            }

            Inventory inventory = item.getProduct().getInventory();

            if (inventory == null) {
                throw new BusinessException("Producto sin inventario asociado");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException("Cantidad inválida en ítems");
            }

            if (inventory.getStock() < item.getQuantity()) {
                throw new BusinessException(
                        "Stock insuficiente para el producto: " + item.getProduct().getName()
                );
            }

            if (inventory.getStock() - item.getQuantity() < inventory.getMinStock()) {
                throw new BusinessException(
                        "El pedido deja el stock por debajo del mínimo: " + item.getProduct().getName()
                );
            }
        }

        for (OrderItem item : order.getItems()) {
            Inventory inventory = item.getProduct().getInventory();
            inventory.setStock(inventory.getStock() - item.getQuantity());
            inventoryRepository.save(inventory);
        }


        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem item : order.getItems()) {

            BigDecimal unitPrice = item.getProduct().getPrice();
            item.setUnitPrice(unitPrice);

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setSubtotal(subtotal);

            total = total.add(subtotal);
        }

        order.setTotal(total);


        order.setStatus(OrderStatus.PAID);

        orderRepository.save(order);

        saveHistory(order, OrderStatus.PAID);
    }


    @Override
    public void shipOrder(Long orderId) {

        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("Solo pedidos en estado PAID pueden enviarse");
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

                if (order.getItems() != null) {
                    for (OrderItem item : order.getItems()) {
                        Inventory inventory = item.getProduct().getInventory();

                        if (inventory != null) {
                            inventory.setStock(inventory.getStock() + item.getQuantity());
                            inventoryRepository.save(inventory);
                        }
                    }
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
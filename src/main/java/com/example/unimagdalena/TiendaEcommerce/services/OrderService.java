package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.Order;
import com.example.unimagdalena.TiendaEcommerce.entities.OrderItem;

import java.util.List;

public interface OrderService {

    Order createOrder(Long customerId, Long addressId, List<OrderItem> items);

    Order getOrderById(Long id);

    List<Order> getAllOrders();

    void payOrder(Long orderId);

    void shipOrder(Long orderId);

    void deliverOrder(Long orderId);

    void cancelOrder(Long orderId);
}
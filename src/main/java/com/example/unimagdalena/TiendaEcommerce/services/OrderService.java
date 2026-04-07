package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.*;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getAllOrders();

    void payOrder(Long orderId);

    void shipOrder(Long orderId);

    void deliverOrder(Long orderId);

    void cancelOrder(Long orderId);
}
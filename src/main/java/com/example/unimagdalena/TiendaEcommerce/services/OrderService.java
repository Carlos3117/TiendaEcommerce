package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.Order;

import java.util.List;

public interface OrderService {


    Order createOrder();


    Order getOrderById(Long id);

    List<Order> getAllOrders();


    void payOrder(Long orderId);


    void shipOrder(Long orderId);

    void deliverOrder(Long orderId);


    void cancelOrder(Long orderId);
}
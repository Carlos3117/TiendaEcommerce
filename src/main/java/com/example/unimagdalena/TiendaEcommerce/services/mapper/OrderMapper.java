package com.example.unimagdalena.TiendaEcommerce.services.mapper;

import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.*;
import com.example.unimagdalena.TiendaEcommerce.entities.*;

import java.util.List;

public class OrderMapper {

    public static OrderResponse toResponse(Order o) {

        var items = o.getItems() == null ? List.<OrderItemResponse>of()
                : o.getItems().stream()
                .map(OrderMapper::toItemResponse)
                .toList();

        return new OrderResponse(
                o.getId(),
                o.getCustomer().getId(),
                o.getAddress().getId(),
                o.getStatus().name(),
                o.getTotal(),
                o.getCreatedAt(),
                items
        );
    }

    public static OrderItemResponse toItemResponse(OrderItem i) {
        return new OrderItemResponse(
                i.getProduct().getId(),
                i.getProduct().getName(),
                i.getQuantity(),
                i.getUnitPrice(),
                i.getSubtotal()
        );
    }
}
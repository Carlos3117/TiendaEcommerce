package com.example.unimagdalena.TiendaEcommerce.services.mapper;

import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.*;
import com.example.unimagdalena.TiendaEcommerce.entities.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IOrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "addressId", source = "address.id")
    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    @Mapping(target = "items", source = "items")
    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderItemResponse toItemResponse(OrderItem item);

    List<OrderItemResponse> toItemResponses(List<OrderItem> items);
}
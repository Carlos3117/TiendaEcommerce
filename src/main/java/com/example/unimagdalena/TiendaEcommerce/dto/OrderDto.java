package com.example.unimagdalena.TiendaEcommerce.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    public record CreateOrderItemRequest(

            @NotNull(message = "El producto es obligatorio")
            Long productId,

            @NotNull
            @Min(value = 1, message = "La cantidad debe ser mayor a 0")
            Integer quantity

    ) implements Serializable {}

    public record CreateOrderRequest(

            @NotNull(message = "El cliente es obligatorio")
            Long customerId,

            @NotNull(message = "La dirección es obligatoria")
            Long addressId,

            @NotEmpty(message = "El pedido debe tener al menos un ítem")
            List<CreateOrderItemRequest> items

    ) implements Serializable {}

    public record CancelOrderRequest(

            @NotNull
            Long orderId

    ) implements Serializable {}

    public record OrderItemResponse(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) implements Serializable {}

    public record OrderResponse(
            Long id,
            Long customerId,
            Long addressId,
            String status,
            BigDecimal total,
            LocalDateTime createdAt,
            List<OrderItemResponse> items
    ) implements Serializable {}
}
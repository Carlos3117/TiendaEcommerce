package com.example.unimagdalena.TiendaEcommerce.controllers;

import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.CreateOrderItemRequest;
import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.CreateOrderRequest;
import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.OrderItemResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.OrderResponse;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.services.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mvc;

    private final ObjectMapper om = new ObjectMapper();

    @MockitoBean
    OrderService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new CreateOrderRequest(
                5L,
                8L,
                List.of(
                        new CreateOrderItemRequest(10L, 2),
                        new CreateOrderItemRequest(11L, 1)
                )
        );

        var resp = new OrderResponse(
                100L,
                5L,
                8L,
                "CREATED",
                new BigDecimal("55000"),
                LocalDateTime.of(2026, 4, 12, 10, 30),
                List.of(
                        new OrderItemResponse(10L, "Libro", 2, new BigDecimal("20000"), new BigDecimal("40000")),
                        new OrderItemResponse(11L, "Lapiz", 1, new BigDecimal("15000"), new BigDecimal("15000"))
                )
        );

        when(service.createOrder(any(CreateOrderRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.endsWith("/api/orders/100")))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.customerId").value(5))
                .andExpect(jsonPath("$.addressId").value(8))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.total").value(55000))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].productId").value(10))
                .andExpect(jsonPath("$.items[0].productName").value("Libro"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[1].productId").value(11))
                .andExpect(jsonPath("$.items[1].productName").value("Lapiz"));
    }

    @Test
    void create_shouldReturn400WhenRequestIsInvalid() throws Exception {
        var req = new CreateOrderRequest(
                null,
                null,
                List.of()
        );

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void create_shouldReturn400WhenBusinessRuleFails() throws Exception {
        var req = new CreateOrderRequest(
                5L,
                8L,
                List.of(new CreateOrderItemRequest(10L, 2))
        );

        when(service.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new BusinessException("El cliente no está activo"));

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El cliente no está activo"));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(service.getOrderById(5L)).thenReturn(
                new OrderResponse(
                        5L,
                        3L,
                        9L,
                        "PAID",
                        new BigDecimal("40000"),
                        LocalDateTime.of(2026, 4, 10, 9, 0),
                        List.of(
                                new OrderItemResponse(7L, "Cuaderno", 2, new BigDecimal("20000"), new BigDecimal("40000"))
                        )
                )
        );

        mvc.perform(get("/api/orders/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.customerId").value(3))
                .andExpect(jsonPath("$.addressId").value(9))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.total").value(40000))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(7))
                .andExpect(jsonPath("$.items[0].productName").value("Cuaderno"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(service.getOrderById(99L))
                .thenThrow(new ResourceNotFoundException("Pedido no encontrado"));

        mvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pedido no encontrado"));
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        when(service.getAllOrders()).thenReturn(List.of(
                new OrderResponse(
                        1L,
                        2L,
                        3L,
                        "CREATED",
                        new BigDecimal("20000"),
                        LocalDateTime.of(2026, 4, 1, 8, 0),
                        List.of()
                ),
                new OrderResponse(
                        2L,
                        4L,
                        5L,
                        "PAID",
                        new BigDecimal("50000"),
                        LocalDateTime.of(2026, 4, 2, 9, 0),
                        List.of()
                )
        ));

        mvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].status").value("PAID"));
    }

    @Test
    void pay_shouldReturn200() throws Exception {
        mvc.perform(put("/api/orders/3/pay"))
                .andExpect(status().isOk());

        verify(service).payOrder(3L);
    }

    @Test
    void pay_shouldReturn400WhenStockIsInsufficient() throws Exception {
        doThrow(new BusinessException("Stock insuficiente"))
                .when(service).payOrder(3L);

        mvc.perform(put("/api/orders/3/pay"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Stock insuficiente"));
    }

    @Test
    void pay_shouldReturn404WhenOrderDoesNotExist() throws Exception {
        doThrow(new ResourceNotFoundException("Pedido no encontrado"))
                .when(service).payOrder(99L);

        mvc.perform(put("/api/orders/99/pay"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pedido no encontrado"));
    }

    @Test
    void ship_shouldReturn200() throws Exception {
        mvc.perform(put("/api/orders/3/ship"))
                .andExpect(status().isOk());

        verify(service).shipOrder(3L);
    }

    @Test
    void ship_shouldReturn400WhenTransitionIsInvalid() throws Exception {
        doThrow(new BusinessException("Solo pedidos pagados pueden enviarse"))
                .when(service).shipOrder(3L);

        mvc.perform(put("/api/orders/3/ship"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Solo pedidos pagados pueden enviarse"));
    }

    @Test
    void deliver_shouldReturn200() throws Exception {
        mvc.perform(put("/api/orders/3/deliver"))
                .andExpect(status().isOk());

        verify(service).deliverOrder(3L);
    }

    @Test
    void deliver_shouldReturn400WhenTransitionIsInvalid() throws Exception {
        doThrow(new BusinessException("Solo pedidos enviados pueden entregarse"))
                .when(service).deliverOrder(3L);

        mvc.perform(put("/api/orders/3/deliver"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Solo pedidos enviados pueden entregarse"));
    }

    @Test
    void cancel_shouldReturn200() throws Exception {
        mvc.perform(put("/api/orders/3/cancel"))
                .andExpect(status().isOk());

        verify(service).cancelOrder(3L);
    }

    @Test
    void cancel_shouldReturn400WhenBusinessRuleFails() throws Exception {
        doThrow(new BusinessException("No se puede cancelar un pedido que ya fue enviado"))
                .when(service).cancelOrder(3L);

        mvc.perform(put("/api/orders/3/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se puede cancelar un pedido que ya fue enviado"));
    }

    @Test
    void cancel_shouldReturn404WhenOrderDoesNotExist() throws Exception {
        doThrow(new ResourceNotFoundException("Pedido no encontrado"))
                .when(service).cancelOrder(99L);

        mvc.perform(put("/api/orders/99/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pedido no encontrado"));
    }
}
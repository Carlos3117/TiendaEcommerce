package com.example.unimagdalena.TiendaEcommerce.controllers;

import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.CreateOrderRequest;
import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.OrderResponse;
import com.example.unimagdalena.TiendaEcommerce.services.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var body = service.createOrder(request);
        var location = uriBuilder.path("/api/orders/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(service.getOrderById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(service.getAllOrders());
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<Void> pay(@PathVariable @Positive Long id) {
        service.payOrder(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/ship")
    public ResponseEntity<Void> ship(@PathVariable @Positive Long id) {
        service.shipOrder(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/deliver")
    public ResponseEntity<Void> deliver(@PathVariable @Positive Long id) {
        service.deliverOrder(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable @Positive Long id) {
        service.cancelOrder(id);
        return ResponseEntity.ok().build();
    }
}
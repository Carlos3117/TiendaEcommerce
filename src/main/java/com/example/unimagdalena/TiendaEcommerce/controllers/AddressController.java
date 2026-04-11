package com.example.unimagdalena.TiendaEcommerce.controllers;

import com.example.unimagdalena.TiendaEcommerce.dto.AddressDto.AddressResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.AddressDto.CreateAddressRequest;
import com.example.unimagdalena.TiendaEcommerce.dto.AddressDto.UpdateAddressRequest;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ValidationException;
import com.example.unimagdalena.TiendaEcommerce.services.AddressService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class AddressController {

    private final AddressService service;

    @PostMapping("/api/customers/{customerId}/addresses")
    public ResponseEntity<AddressResponse> create(
            @PathVariable @Positive Long customerId,
            @Valid @RequestBody CreateAddressRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        if (!customerId.equals(request.customerId())) {
            throw new ValidationException("El customerId del body no coincide con el customerId de la ruta");
        }

        var body = service.createAddress(request);
        var location = uriBuilder.path("/api/addresses/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/api/customers/{customerId}/addresses")
    public ResponseEntity<List<AddressResponse>> getByCustomer(@PathVariable @Positive Long customerId) {
        return ResponseEntity.ok(service.getAddressesByCustomer(customerId));
    }

    @GetMapping("/api/addresses/{id}")
    public ResponseEntity<AddressResponse> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(service.getAddressById(id));
    }

    @PutMapping("/api/addresses/{id}")
    public ResponseEntity<AddressResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        return ResponseEntity.ok(service.updateAddress(id, request));
    }

    @DeleteMapping("/api/addresses/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        service.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}
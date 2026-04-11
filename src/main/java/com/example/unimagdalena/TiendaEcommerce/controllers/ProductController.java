package com.example.unimagdalena.TiendaEcommerce.api;

import com.example.unimagdalena.TiendaEcommerce.dto.InventoryDto.InventoryResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.InventoryDto.UpdateInventoryRequest;
import com.example.unimagdalena.TiendaEcommerce.dto.ProductDto.CreateProductRequest;
import com.example.unimagdalena.TiendaEcommerce.dto.ProductDto.ProductResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.ProductDto.UpdateProductRequest;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ValidationException;
import com.example.unimagdalena.TiendaEcommerce.services.InventoryService;
import com.example.unimagdalena.TiendaEcommerce.services.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody CreateProductRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var body = productService.createProduct(request);
        var location = uriBuilder.path("/api/products/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PutMapping("/{id}/inventory")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateInventoryRequest request
    ) {
        if (!id.equals(request.productId())) {
            throw new ValidationException("El productId del body no coincide con el id de la ruta");
        }
        return ResponseEntity.ok(inventoryService.updateInventory(request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductResponse> changeStatus(
            @PathVariable @Positive Long id,
            @RequestParam @NotNull Boolean active
    ) {
        return ResponseEntity.ok(productService.changeProductStatus(id, active));
    }
}
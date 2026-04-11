package com.example.unimagdalena.TiendaEcommerce.api;

import com.example.unimagdalena.TiendaEcommerce.dto.CategoryDto.CategoryResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.CategoryDto.CreateCategoryRequest;
import com.example.unimagdalena.TiendaEcommerce.dto.CategoryDto.UpdateCategoryRequest;
import com.example.unimagdalena.TiendaEcommerce.services.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService service;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CreateCategoryRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var body = service.createCategory(request);
        var location = uriBuilder.path("/api/categories/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(service.getCategoryById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(service.getAllCategories());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        return ResponseEntity.ok(service.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        service.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
package com.example.unimagdalena.TiendaEcommerce.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

public class ProductDto {

    public record CreateProductRequest(

            @NotBlank(message = "El nombre es obligatorio")
            String name,

            @NotBlank(message = "El SKU es obligatorio")
            String sku,

            @NotNull(message = "El precio es obligatorio")
            @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
            BigDecimal price,

            @NotNull(message = "La categoría es obligatoria")
            Long categoryId

    ) implements Serializable {}

    public record UpdateProductRequest(

            String name,

            String sku,

            @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
            BigDecimal price,

            Long categoryId

    ) implements Serializable {}

    public record ProductResponse(
            Long id,
            String name,
            String sku,
            BigDecimal price,
            Boolean active,
            Long categoryId
    ) implements Serializable {}
}
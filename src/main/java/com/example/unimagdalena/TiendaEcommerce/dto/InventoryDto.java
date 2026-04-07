package com.example.unimagdalena.TiendaEcommerce.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;

public class InventoryDto {

    public record UpdateInventoryRequest(

            @NotNull(message = "El producto es obligatorio")
            Long productId,

            @NotNull
            @Min(value = 0, message = "El stock no puede ser negativo")
            Integer stock,

            @NotNull
            @Min(value = 0, message = "El stock mínimo no puede ser negativo")
            Integer minStock

    ) implements Serializable {}

    public record InventoryResponse(
            Long productId,
            Integer stock,
            Integer minStock
    ) implements Serializable {}
}
package com.example.unimagdalena.TiendaEcommerce.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;

public class CategoryDto {

    public record CreateCategoryRequest(

            @NotBlank(message = "El nombre es obligatorio")
            String name

    ) implements Serializable {}

    public record CategoryResponse(
            Long id,
            String name
    ) implements Serializable {}
}
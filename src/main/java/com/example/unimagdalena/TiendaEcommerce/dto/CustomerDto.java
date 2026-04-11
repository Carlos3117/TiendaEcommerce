
package com.example.unimagdalena.TiendaEcommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

public class CustomerDto {

    public record CreateCustomerRequest(

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 100)
            String firstName,

            @NotBlank(message = "El apellido es obligatorio")
            @Size(max = 100)
            String lastName,

            @NotBlank(message = "El email es obligatorio")
            @Email(message = "Formato de email inválido")
            String email,

            @NotBlank(message = "El teléfono es obligatorio")
            @Size(max = 20)
            String phoneNumber

    ) implements Serializable {}

    public record UpdateCustomerRequest(

            @NotBlank
            String firstName,

            @NotBlank
            String lastName,

            @NotBlank
            @Email
            String email

    ) implements Serializable {}

    public record CustomerResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String status
    ) implements Serializable {}
}

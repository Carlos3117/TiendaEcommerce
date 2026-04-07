package com.example.unimagdalena.TiendaEcommerce.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;

public class AddressDto {

    public record CreateAddressRequest(

            @NotBlank(message = "La dirección es obligatoria")
            String street,

            @NotBlank(message = "La ciudad es obligatoria")
            String city,

            @NotNull(message = "El cliente es obligatorio")
            Long customerId

    ) implements Serializable {}

    public record UpdateAddressRequest(

            String street,
            String city

    ) implements Serializable {}


    public record AddressResponse(
            Long id,
            String street,
            String city,
            Long customerId
    ) implements Serializable {}
}
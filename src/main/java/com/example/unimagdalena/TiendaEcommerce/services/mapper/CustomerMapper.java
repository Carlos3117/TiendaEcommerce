package com.example.unimagdalena.TiendaEcommerce.services.mapper;

import com.example.unimagdalena.TiendaEcommerce.dto.CustomerDto.UpdateCustomerRequest;
import com.example.unimagdalena.TiendaEcommerce.entities.Customer;

public class CustomerMapper {

    public static void patch(Customer entity, UpdateCustomerRequest req) {
        if (req.firstName() != null) entity.setFirstName(req.firstName());
        if (req.lastName() != null) entity.setLastName(req.lastName());
        if (req.email() != null) entity.setEmail(req.email());
    }
}
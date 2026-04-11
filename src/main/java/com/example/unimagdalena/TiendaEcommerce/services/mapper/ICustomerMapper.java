package com.example.unimagdalena.TiendaEcommerce.services.mapper;

import com.example.unimagdalena.TiendaEcommerce.dto.CustomerDto.*;
import com.example.unimagdalena.TiendaEcommerce.entities.Customer;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ICustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Customer toEntity(CreateCustomerRequest req);

    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    CustomerResponse toResponse(Customer entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(@MappingTarget Customer target, UpdateCustomerRequest changes);
}
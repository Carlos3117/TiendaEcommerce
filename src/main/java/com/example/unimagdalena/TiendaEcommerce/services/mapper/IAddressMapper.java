package com.example.unimagdalena.TiendaEcommerce.services.mapper;

import com.example.unimagdalena.TiendaEcommerce.dto.AddressDto.*;
import com.example.unimagdalena.TiendaEcommerce.entities.Address;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IAddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    Address toEntity(CreateAddressRequest req);

    @Mapping(target = "customerId", source = "customer.id")
    AddressResponse toResponse(Address entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(@MappingTarget Address target, UpdateAddressRequest changes);
}
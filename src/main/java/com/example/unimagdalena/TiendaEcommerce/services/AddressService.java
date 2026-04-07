package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.dto.AddressDto.*;

import java.util.List;

public interface AddressService {

    AddressResponse createAddress(CreateAddressRequest request);

    AddressResponse getAddressById(Long id);

    List<AddressResponse> getAddressesByCustomer(Long customerId);

    AddressResponse updateAddress(Long id, UpdateAddressRequest request);

    void deleteAddress(Long id);
}
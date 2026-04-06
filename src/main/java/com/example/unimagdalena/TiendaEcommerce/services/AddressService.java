package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.Address;

import java.util.List;

public interface AddressService {

    Address createAddress(Address address);

    Address getAddressById(Long id);

    List<Address> getAddressesByCustomer(Long customerId);

    Address updateAddress(Long id, Address address);

    void deleteAddress(Long id);
}
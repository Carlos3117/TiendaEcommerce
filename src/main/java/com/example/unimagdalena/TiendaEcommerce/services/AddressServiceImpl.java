package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.Address;
import com.example.unimagdalena.TiendaEcommerce.entities.Customer;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.repositories.AddressRepository;
import com.example.unimagdalena.TiendaEcommerce.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;


    @Override
    public Address createAddress(Address address) {

        if (address.getCustomer() == null || address.getCustomer().getId() == null) {
            throw new BusinessException("El cliente es obligatorio");
        }

        Customer customer = customerRepository.findById(address.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        if (address.getStreet() == null || address.getStreet().isBlank()) {
            throw new BusinessException("La dirección es obligatoria");
        }

        if (address.getCity() == null || address.getCity().isBlank()) {
            throw new BusinessException("La ciudad es obligatoria");
        }

        address.setCustomer(customer);

        return addressRepository.save(address);
    }

    @Override
    @Transactional(readOnly = true)
    public Address getAddressById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));
    }


    @Override
    @Transactional(readOnly = true)
    public List<Address> getAddressesByCustomer(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        return addressRepository.findByCustomer(customer);
    }


    @Override
    public Address updateAddress(Long id, Address updated) {

        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

        if (updated.getStreet() != null && !updated.getStreet().isBlank()) {
            existing.setStreet(updated.getStreet());
        }

        if (updated.getCity() != null && !updated.getCity().isBlank()) {
            existing.setCity(updated.getCity());
        }

        return addressRepository.save(existing);
    }


    @Override
    public void deleteAddress(Long id) {

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

        addressRepository.delete(address);
    }
}
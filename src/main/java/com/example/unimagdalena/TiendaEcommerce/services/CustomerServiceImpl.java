package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.Customer;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Customer createCustomer(Customer customer) {

        if (customer.getFirstName() == null || customer.getFirstName().isBlank()) {
            throw new BusinessException("El nombre es obligatorio");
        }

        if (customer.getLastName() == null || customer.getLastName().isBlank()) {
            throw new BusinessException("El apellido es obligatorio");
        }

        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new BusinessException("El email es obligatorio");
        }

        customerRepository.findByEmail(customer.getEmail())
                .ifPresent(c -> {
                    throw new BusinessException("El email ya está registrado");
                });

        return customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {

        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {

        return customerRepository.findAll();
    }

    @Override
    public Customer updateCustomer(Long id, Customer customer) {

        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        if (customer.getFirstName() == null || customer.getFirstName().isBlank()) {
            throw new BusinessException("El nombre es obligatorio");
        }

        if (customer.getLastName() == null || customer.getLastName().isBlank()) {
            throw new BusinessException("El apellido es obligatorio");
        }

        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new BusinessException("El email es obligatorio");
        }

        customerRepository.findByEmail(customer.getEmail())
                .ifPresent(c -> {
                    if (!c.getId().equals(id)) {
                        throw new BusinessException("El email ya está en uso");
                    }
                });

        existing.setFirstName(customer.getFirstName());
        existing.setLastName(customer.getLastName());
        existing.setEmail(customer.getEmail());

        return customerRepository.save(existing);
    }

    @Override
    public void deleteCustomer(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        customerRepository.delete(customer);
    }
}
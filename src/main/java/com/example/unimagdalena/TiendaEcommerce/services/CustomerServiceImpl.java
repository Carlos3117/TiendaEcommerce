package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.dto.CustomerDto.*;
import com.example.unimagdalena.TiendaEcommerce.entities.Customer;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.services.mapper.ICustomerMapper;
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
    private final ICustomerMapper customerMapper;

    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {

        customerRepository.findByEmail(request.email())
                .ifPresent(c -> {
                    throw new BusinessException("El email ya está registrado");
                });

        Customer customer = customerMapper.toEntity(request);

        Customer saved = customerRepository.save(customer);

        return customerMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {

        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {

        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        customerRepository.findByEmail(request.email())
                .ifPresent(c -> {
                    if (!c.getId().equals(id)) {
                        throw new BusinessException("El email ya está en uso");
                    }
                });

        customerMapper.patch(existing, request);

        Customer updated = customerRepository.save(existing);

        return customerMapper.toResponse(updated);
    }

    @Override
    public void deleteCustomer(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        customerRepository.delete(customer);
    }
}
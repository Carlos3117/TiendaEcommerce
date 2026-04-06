package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.Address;
import com.example.unimagdalena.TiendaEcommerce.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address , Long> {
    List<Address> findByCustomer(Customer customer);

}

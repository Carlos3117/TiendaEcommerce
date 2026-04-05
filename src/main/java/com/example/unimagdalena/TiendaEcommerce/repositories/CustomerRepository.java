package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {


}

package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Long> {

}

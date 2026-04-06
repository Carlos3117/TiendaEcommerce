package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.Category;
import com.example.unimagdalena.TiendaEcommerce.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {
   Optional<Product> findBySku(String sku);
   Optional<Product> findById(Long Id);
   List<Product> findByCategoryAndActiveTrue(Category category);
   List<Product> findByCategory(Category category);
}

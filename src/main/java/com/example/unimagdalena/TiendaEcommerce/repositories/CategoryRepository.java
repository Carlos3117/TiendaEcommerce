package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

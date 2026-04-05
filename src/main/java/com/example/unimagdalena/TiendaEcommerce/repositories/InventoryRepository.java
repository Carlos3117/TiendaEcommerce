package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.Inventory;
import com.example.unimagdalena.TiendaEcommerce.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    // Productos con bajo stock
    @Query("""
    SELECT i.product
    FROM Inventory i
    where i.stock < i.minStock 
""")
    List<Product> findProductsWithLowStock();

}

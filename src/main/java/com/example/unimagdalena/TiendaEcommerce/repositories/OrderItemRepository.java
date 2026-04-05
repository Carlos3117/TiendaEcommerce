package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.Category;
import com.example.unimagdalena.TiendaEcommerce.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
    SELECT oi.product, SUM(oi.quantity)
    FROM OrderItem oi
    JOIN oi.order o
    WHERE o.createdAt BETWEEN :startDate AND :endDate
    GROUP BY oi.product
    ORDER BY SUM(oi.quantity) DESC 
""")
    List<Object[]> findBestSellingProductsForRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
            );

    @Query("""
    SELECT oc, SUM(oi.quantity)
    FROM OrderItem oi
    JOIN oi.product op
    JOIN op.category oc
    GROUP BY oc
    ORDER BY SUM(oi.quantity) DESC 
""") List<Object[]> findTopCategorySellers();
}

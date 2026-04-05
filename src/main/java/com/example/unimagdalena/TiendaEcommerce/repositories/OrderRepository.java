package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.Order;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
    SELECT o FROM Order o
    WHERE (:customerId IS NULL OR o.customer.id = :customerId)
    AND (:status IS NULL OR o.status = :status)
    AND (o.createdAt >= COALESCE(:startDate, o.createdAt))
    AND (o.createdAt <= COALESCE(:endDate, o.createdAt))
    AND (o.total >= COALESCE(:minTotal, o.total))
    AND (o.total <= COALESCE(:maxTotal, o.total))
    """)
    List<Order> findOrdersWithFilters(
            @Param("customerId") Long customerId,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minTotal") BigDecimal minTotal,
            @Param("maxTotal") BigDecimal maxTotal
    );

    @Query("""
        SELECT 
            EXTRACT(YEAR FROM o.createdAt),
            EXTRACT(MONTH FROM o.createdAt),
            SUM(o.total)
        FROM Order o
        GROUP BY 
            EXTRACT(YEAR FROM o.createdAt),
            EXTRACT(MONTH FROM o.createdAt)
        ORDER BY 
            EXTRACT(YEAR FROM o.createdAt),
            EXTRACT(MONTH FROM o.createdAt)
    """)
    List<Object[]> getMonthlyIncome();

    @Query("""
        SELECT o.customer, SUM(o.total)
        FROM Order o
        GROUP BY o.customer
        ORDER BY SUM(o.total) DESC
    """)
    List<Object[]> findTopSpendingCustomers();
}
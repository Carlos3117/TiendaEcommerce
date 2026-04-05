package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.Customer;
import com.example.unimagdalena.TiendaEcommerce.entities.Order;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer(Customer customer);

    @Query("""
SELECT o FROM Order o
WHERE (:customer IS NULL OR o.customer = :customer)
AND (:status IS NULL OR o.status = :status)
AND (:startDate IS NULL OR o.createdAt >= :startDate)
AND (:endDate IS NULL OR o.createdAt <= :endDate)
AND (:minTotal IS NULL OR o.total >= :minTotal)
AND (:maxTotal IS NULL OR o.total <= :maxTotal)
""")
    List<Order> findOrdersWithFilters(
            @Param("customer") Customer customer,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minTotal") BigDecimal minTotal,
            @Param("maxTotal") BigDecimal maxTotal
    );

}

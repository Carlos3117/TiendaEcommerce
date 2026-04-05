package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.Order;
import com.example.unimagdalena.TiendaEcommerce.entities.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository <OrderStatusHistory, Long> {
    //Historial de cambios de un pedido.
    @Query("""
    SELECT osh
    FROM OrderStatusHistory osh
    WHERE osh.order = :order
    ORDER BY osh.changedAt ASC
""")
    List<OrderStatusHistory> findHistorialByOrder(@Param("order") Order order);
}

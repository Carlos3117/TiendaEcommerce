package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository <OrderStatusHistory, Long> {

}

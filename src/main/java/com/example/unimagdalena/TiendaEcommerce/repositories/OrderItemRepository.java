package com.example.unimagdalena.TiendaEcommerce.repositories;

import com.example.unimagdalena.TiendaEcommerce.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}

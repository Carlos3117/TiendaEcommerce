package com.example.unimagdalena.TiendaEcommerce.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;

@Getter
@Setter
@Entity
@Table(name = "order_status_history")
@NoArgsConstructor
@AllArgsConstructor

public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @PrePersist
    public void prePersist() {
        this.changedAt = LocalDateTime.now();
    }
}

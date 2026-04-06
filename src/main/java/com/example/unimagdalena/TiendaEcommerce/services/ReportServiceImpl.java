package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.Customer;
import com.example.unimagdalena.TiendaEcommerce.entities.Order;
import com.example.unimagdalena.TiendaEcommerce.entities.Product;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.repositories.CustomerRepository;
import com.example.unimagdalena.TiendaEcommerce.repositories.InventoryRepository;
import com.example.unimagdalena.TiendaEcommerce.repositories.OrderItemRepository;
import com.example.unimagdalena.TiendaEcommerce.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public List<Product> getLowStockProducts() {
        return inventoryRepository.findProductsWithInsufficientStock();
    }

    @Override
    public List<Order> getOrdersByFilters(Long customerId,
                                          String status,
                                          LocalDateTime startDate,
                                          LocalDateTime endDate,
                                          BigDecimal minTotal,
                                          BigDecimal maxTotal) {

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("La fecha inicial no puede ser mayor que la final");
        }

        if (minTotal != null && maxTotal != null && minTotal.compareTo(maxTotal) > 0) {
            throw new BusinessException("El monto mínimo no puede ser mayor que el máximo");
        }

        OrderStatus orderStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                orderStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Estado inválido: " + status);
            }
        }

        return orderRepository.findOrdersWithFilters(
                customerId,
                orderStatus,
                startDate,
                endDate,
                minTotal,
                maxTotal
        );
    }

    @Override
    public List<Object[]> getTopSellingProducts(LocalDateTime startDate,
                                                LocalDateTime endDate) {

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("Rango de fechas inválido");
        }

        return orderItemRepository.findBestSellingProductsForRange(startDate, endDate);
    }

    @Override
    public List<Object[]> getMonthlyIncome() {
        return orderRepository.getMonthlyIncome();
    }

    @Override
    public List<Object[]> getTopCustomers() {
        return orderRepository.findTopSpendingCustomers();
    }
}
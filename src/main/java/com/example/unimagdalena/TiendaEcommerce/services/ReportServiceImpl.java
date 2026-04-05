package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.Customer;
import com.example.unimagdalena.TiendaEcommerce.entities.Order;
import com.example.unimagdalena.TiendaEcommerce.entities.Product;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
import com.example.unimagdalena.TiendaEcommerce.repositories.InventoryRepository;
import com.example.unimagdalena.TiendaEcommerce.repositories.OrderItemRepository;
import com.example.unimagdalena.TiendaEcommerce.repositories.OrderRepository;
import com.example.unimagdalena.TiendaEcommerce.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class ReportServiceImpl implements ReportService{

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

        OrderStatus orderStatus = null;

        if (status != null) {
            orderStatus = OrderStatus.valueOf(status);
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
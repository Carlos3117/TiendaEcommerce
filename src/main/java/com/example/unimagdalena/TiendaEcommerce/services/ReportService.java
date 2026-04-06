package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.Order;
import com.example.unimagdalena.TiendaEcommerce.entities.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {

    List<Product> getLowStockProducts();

    List<Order> getOrdersByFilters(Long customerId,
                                   String status,
                                   LocalDateTime startDate,
                                   LocalDateTime endDate,
                                   BigDecimal minTotal,
                                   BigDecimal maxTotal);

    List<Object[]> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate);

    List<Object[]> getMonthlyIncome();

    List<Object[]> getTopCustomers();
}
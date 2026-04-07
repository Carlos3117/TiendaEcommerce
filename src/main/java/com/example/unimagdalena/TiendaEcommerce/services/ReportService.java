package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.OrderResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.ReportDto.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {

    List<LowStockProductResponse> getLowStockProducts();

    List<OrderResponse> getOrdersByFilters(Long customerId,
                                           String status,
                                           LocalDateTime startDate,
                                           LocalDateTime endDate,
                                           BigDecimal minTotal,
                                           BigDecimal maxTotal);

    List<BestSellingProductResponse> getTopSellingProducts(LocalDateTime startDate,
                                                           LocalDateTime endDate);

    List<MonthlyIncomeResponse> getMonthlyIncome();

    List<TopCustomerResponse> getTopCustomers();
}
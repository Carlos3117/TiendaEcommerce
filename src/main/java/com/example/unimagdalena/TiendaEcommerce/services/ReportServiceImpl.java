package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.OrderResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.ReportDto.*;
import com.example.unimagdalena.TiendaEcommerce.entities.*;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.services.mapper.OrderMapper;
import com.example.unimagdalena.TiendaEcommerce.services.mapper.ReportMapper;
import com.example.unimagdalena.TiendaEcommerce.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public List<LowStockProductResponse> getLowStockProducts() {

        return inventoryRepository.findLowStockProducts()
                .stream()
                .map(inv -> ReportMapper.toLowStock(
                        inv.getProduct(),
                        inv.getStock(),
                        inv.getMinStock()
                ))
                .toList();
    }

    @Override
    public List<OrderResponse> getOrdersByFilters(Long customerId,
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
                )
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @Override
    public List<BestSellingProductResponse> getTopSellingProducts(LocalDateTime startDate,
                                                                  LocalDateTime endDate) {

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("Rango de fechas inválido");
        }

        return orderItemRepository.findBestSellingProductsForRange(startDate, endDate)
                .stream()
                .map(obj -> ReportMapper.toBestSelling(
                        (Product) obj[0],
                        (Long) obj[1]
                ))
                .toList();
    }

    @Override
    public List<MonthlyIncomeResponse> getMonthlyIncome() {

        return orderRepository.getMonthlyIncome()
                .stream()
                .map(obj -> ReportMapper.toMonthly(
                        (Integer) obj[0],
                        (Integer) obj[1],
                        (BigDecimal) obj[2]
                ))
                .toList();
    }

    @Override
    public List<TopCustomerResponse> getTopCustomers() {

        return orderRepository.findTopSpendingCustomers()
                .stream()
                .map(obj -> ReportMapper.toTopCustomer(
                        (Customer) obj[0],
                        (BigDecimal) obj[1]
                ))
                .toList();
    }
}
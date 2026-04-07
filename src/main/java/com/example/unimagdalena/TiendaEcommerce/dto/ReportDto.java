package com.example.unimagdalena.TiendaEcommerce.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ReportDto {

    public record BestSellingProductResponse(
            Long productId,
            String productName,
            Long totalSold
    ) implements Serializable {}

    public record MonthlyIncomeResponse(
            Integer year,
            Integer month,
            BigDecimal total
    ) implements Serializable {}

    public record TopCustomerResponse(
            Long customerId,
            String customerName,
            BigDecimal totalSpent
    ) implements Serializable {}

    public record LowStockProductResponse(
            Long productId,
            String productName,
            Integer stock,
            Integer minStock
    ) implements Serializable {}
}
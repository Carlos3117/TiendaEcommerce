package com.example.unimagdalena.TiendaEcommerce.services.mapper;

import com.example.unimagdalena.TiendaEcommerce.dto.ReportDto.*;
import com.example.unimagdalena.TiendaEcommerce.entities.Customer;
import com.example.unimagdalena.TiendaEcommerce.entities.Product;

import java.math.BigDecimal;

public class ReportMapper {

    public static BestSellingProductResponse toBestSelling(Product product, Long totalSold) {
        return new BestSellingProductResponse(
                product.getId(),
                product.getName(),
                totalSold
        );
    }

    public static MonthlyIncomeResponse toMonthly(Integer year, Integer month, BigDecimal total) {
        return new MonthlyIncomeResponse(
                year,
                month,
                total
        );
    }

    public static TopCustomerResponse toTopCustomer(Customer customer, BigDecimal totalSpent) {
        return new TopCustomerResponse(
                customer.getId(),
                customer.getFirstName() + " " + customer.getLastName(),
                totalSpent
        );
    }

    public static LowStockProductResponse toLowStock(Product product, Integer stock, Integer minStock) {
        return new LowStockProductResponse(
                product.getId(),
                product.getName(),
                stock,
                minStock
        );
    }
}
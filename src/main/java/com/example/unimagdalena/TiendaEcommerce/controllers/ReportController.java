package com.example.unimagdalena.TiendaEcommerce.api;

import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.OrderResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.ReportDto.BestSellingProductResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.ReportDto.LowStockProductResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.ReportDto.MonthlyIncomeResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.ReportDto.TopCustomerResponse;
import com.example.unimagdalena.TiendaEcommerce.services.ReportService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService service;

    @GetMapping("/low-stock-products")
    public ResponseEntity<List<LowStockProductResponse>> getLowStockProducts() {
        return ResponseEntity.ok(service.getLowStockProducts());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getOrdersByFilters(
            @RequestParam(required = false) @Positive Long customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal
    ) {
        return ResponseEntity.ok(service.getOrdersByFilters(
                customerId, status, startDate, endDate, minTotal, maxTotal
        ));
    }

    @GetMapping("/best-selling-products")
    public ResponseEntity<List<BestSellingProductResponse>> getBestSellingProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(service.getTopSellingProducts(startDate, endDate));
    }

    @GetMapping("/monthly-income")
    public ResponseEntity<List<MonthlyIncomeResponse>> getMonthlyIncome() {
        return ResponseEntity.ok(service.getMonthlyIncome());
    }

    @GetMapping("/top-customers")
    public ResponseEntity<List<TopCustomerResponse>> getTopCustomers() {
        return ResponseEntity.ok(service.getTopCustomers());
    }
}
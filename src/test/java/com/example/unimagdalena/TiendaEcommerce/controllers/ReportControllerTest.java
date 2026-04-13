package com.example.unimagdalena.TiendaEcommerce.controllers;

import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.OrderItemResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.OrderDto.OrderResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.ReportDto.BestSellingProductResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.ReportDto.LowStockProductResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.ReportDto.MonthlyIncomeResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.ReportDto.TopCustomerResponse;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.services.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ReportService service;

    @Test
    void getLowStockProducts_shouldReturn200() throws Exception {
        when(service.getLowStockProducts()).thenReturn(List.of(
                new LowStockProductResponse(1L, "Cuaderno", 2, 5),
                new LowStockProductResponse(2L, "Lapiz", 1, 3)
        ));

        mvc.perform(get("/api/reports/low-stock-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].productName").value("Cuaderno"))
                .andExpect(jsonPath("$[0].stock").value(2))
                .andExpect(jsonPath("$[0].minStock").value(5))
                .andExpect(jsonPath("$[1].productId").value(2))
                .andExpect(jsonPath("$[1].productName").value("Lapiz"));
    }

    @Test
    void getOrdersByFilters_shouldReturn200() throws Exception {
        var item = new OrderItemResponse(
                10L,
                "Libro",
                2,
                new BigDecimal("20000"),
                new BigDecimal("40000")
        );

        var order = new OrderResponse(
                1L,
                5L,
                8L,
                "PAID",
                new BigDecimal("40000"),
                LocalDateTime.of(2026, 4, 11, 10, 30),
                List.of(item)
        );

        when(service.getOrdersByFilters(
                5L,
                "PAID",
                LocalDateTime.parse("2026-04-01T00:00:00"),
                LocalDateTime.parse("2026-04-30T23:59:59"),
                new BigDecimal("10000"),
                new BigDecimal("50000")
        )).thenReturn(List.of(order));

        mvc.perform(get("/api/reports/orders")
                        .param("customerId", "5")
                        .param("status", "PAID")
                        .param("startDate", "2026-04-01T00:00:00")
                        .param("endDate", "2026-04-30T23:59:59")
                        .param("minTotal", "10000")
                        .param("maxTotal", "50000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].customerId").value(5))
                .andExpect(jsonPath("$[0].addressId").value(8))
                .andExpect(jsonPath("$[0].status").value("PAID"))
                .andExpect(jsonPath("$[0].total").value(40000))
                .andExpect(jsonPath("$[0].items.length()").value(1))
                .andExpect(jsonPath("$[0].items[0].productId").value(10))
                .andExpect(jsonPath("$[0].items[0].productName").value("Libro"));
    }

    @Test
    void getOrdersByFilters_shouldReturn400WhenServiceRejectsFilters() throws Exception {
        when(service.getOrdersByFilters(
                5L,
                "PAID",
                LocalDateTime.parse("2026-05-01T00:00:00"),
                LocalDateTime.parse("2026-04-01T00:00:00"),
                new BigDecimal("10000"),
                new BigDecimal("50000")
        )).thenThrow(new BusinessException("La fecha inicial no puede ser mayor que la final"));

        mvc.perform(get("/api/reports/orders")
                        .param("customerId", "5")
                        .param("status", "PAID")
                        .param("startDate", "2026-05-01T00:00:00")
                        .param("endDate", "2026-04-01T00:00:00")
                        .param("minTotal", "10000")
                        .param("maxTotal", "50000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La fecha inicial no puede ser mayor que la final"));
    }

    @Test
    void getBestSellingProducts_shouldReturn200() throws Exception {
        when(service.getTopSellingProducts(
                LocalDateTime.parse("2026-04-01T00:00:00"),
                LocalDateTime.parse("2026-04-30T23:59:59")
        )).thenReturn(List.of(
                new BestSellingProductResponse(1L, "Libro", 15L),
                new BestSellingProductResponse(2L, "Cuaderno", 10L)
        ));

        mvc.perform(get("/api/reports/best-selling-products")
                        .param("startDate", "2026-04-01T00:00:00")
                        .param("endDate", "2026-04-30T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].productName").value("Libro"))
                .andExpect(jsonPath("$[0].totalSold").value(15))
                .andExpect(jsonPath("$[1].productId").value(2))
                .andExpect(jsonPath("$[1].productName").value("Cuaderno"));
    }

    @Test
    void getBestSellingProducts_shouldReturn400WhenRangeIsInvalid() throws Exception {
        when(service.getTopSellingProducts(
                LocalDateTime.parse("2026-05-01T00:00:00"),
                LocalDateTime.parse("2026-04-01T00:00:00")
        )).thenThrow(new BusinessException("Rango de fechas inválido"));

        mvc.perform(get("/api/reports/best-selling-products")
                        .param("startDate", "2026-05-01T00:00:00")
                        .param("endDate", "2026-04-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rango de fechas inválido"));
    }

    @Test
    void getMonthlyIncome_shouldReturn200() throws Exception {
        when(service.getMonthlyIncome()).thenReturn(List.of(
                new MonthlyIncomeResponse(2026, 3, new BigDecimal("150000")),
                new MonthlyIncomeResponse(2026, 4, new BigDecimal("220000"))
        ));

        mvc.perform(get("/api/reports/monthly-income"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].year").value(2026))
                .andExpect(jsonPath("$[0].month").value(3))
                .andExpect(jsonPath("$[0].total").value(150000))
                .andExpect(jsonPath("$[1].year").value(2026))
                .andExpect(jsonPath("$[1].month").value(4))
                .andExpect(jsonPath("$[1].total").value(220000));
    }

    @Test
    void getTopCustomers_shouldReturn200() throws Exception {
        when(service.getTopCustomers()).thenReturn(List.of(
                new TopCustomerResponse(1L, "Ana Lopez", new BigDecimal("300000")),
                new TopCustomerResponse(2L, "Luis Perez", new BigDecimal("180000"))
        ));

        mvc.perform(get("/api/reports/top-customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].customerName").value("Ana Lopez"))
                .andExpect(jsonPath("$[0].totalSpent").value(300000))
                .andExpect(jsonPath("$[1].customerId").value(2))
                .andExpect(jsonPath("$[1].customerName").value("Luis Perez"));
    }
}
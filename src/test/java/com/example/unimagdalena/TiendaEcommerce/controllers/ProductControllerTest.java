package com.example.unimagdalena.TiendaEcommerce.controllers;

import com.example.unimagdalena.TiendaEcommerce.dto.InventoryDto.InventoryResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.InventoryDto.UpdateInventoryRequest;
import com.example.unimagdalena.TiendaEcommerce.dto.ProductDto.CreateProductRequest;
import com.example.unimagdalena.TiendaEcommerce.dto.ProductDto.ProductResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.ProductDto.UpdateProductRequest;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.services.InventoryService;
import com.example.unimagdalena.TiendaEcommerce.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    MockMvc mvc;

    private final ObjectMapper om = new ObjectMapper();

    @MockitoBean
    ProductService productService;

    @MockitoBean
    InventoryService inventoryService;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new CreateProductRequest(
                "Cuaderno",
                "SKU-001",
                new BigDecimal("25000"),
                2L
        );

        var resp = new ProductResponse(
                10L,
                "Cuaderno",
                "SKU-001",
                new BigDecimal("25000"),
                true,
                2L
        );

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.endsWith("/api/products/10")))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Cuaderno"))
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.price").value(25000))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.categoryId").value(2));
    }

    @Test
    void create_shouldReturn400WhenRequestIsInvalid() throws Exception {
        var req = new CreateProductRequest(
                "",
                "",
                new BigDecimal("0"),
                null
        );

        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(productService.getProductById(5L)).thenReturn(
                new ProductResponse(
                        5L,
                        "Libro",
                        "SKU-100",
                        new BigDecimal("45000"),
                        true,
                        1L
                )
        );

        mvc.perform(get("/api/products/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Libro"))
                .andExpect(jsonPath("$.sku").value("SKU-100"))
                .andExpect(jsonPath("$.price").value(45000))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.categoryId").value(1));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(productService.getProductById(99L))
                .thenThrow(new ResourceNotFoundException("Producto no encontrado"));

        mvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Producto no encontrado"));
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(
                new ProductResponse(1L, "Libro", "SKU-100", new BigDecimal("45000"), true, 1L),
                new ProductResponse(2L, "Lapiz", "SKU-101", new BigDecimal("5000"), true, 1L)
        ));

        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Libro"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Lapiz"));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new UpdateProductRequest(
                "Libro actualizado",
                "SKU-200",
                new BigDecimal("50000"),
                3L
        );

        var resp = new ProductResponse(
                3L,
                "Libro actualizado",
                "SKU-200",
                new BigDecimal("50000"),
                true,
                3L
        );

        when(productService.updateProduct(3L, req)).thenReturn(resp);

        mvc.perform(put("/api/products/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Libro actualizado"))
                .andExpect(jsonPath("$.sku").value("SKU-200"))
                .andExpect(jsonPath("$.price").value(50000))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.categoryId").value(3));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        var req = new UpdateProductRequest(
                "Libro actualizado",
                "SKU-200",
                new BigDecimal("50000"),
                3L
        );

        when(productService.updateProduct(50L, req))
                .thenThrow(new ResourceNotFoundException("Producto no encontrado"));

        mvc.perform(put("/api/products/50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Producto no encontrado"));
    }

    @Test
    void updateInventory_shouldReturn200() throws Exception {
        var req = new UpdateInventoryRequest(
                3L,
                20,
                5
        );

        var resp = new InventoryResponse(
                3L,
                20,
                5
        );

        when(inventoryService.updateInventory(req)).thenReturn(resp);

        mvc.perform(put("/api/products/3/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(3))
                .andExpect(jsonPath("$.stock").value(20))
                .andExpect(jsonPath("$.minStock").value(5));
    }

    @Test
    void updateInventory_shouldReturn400WhenProductIdDoesNotMatchPath() throws Exception {
        var req = new UpdateInventoryRequest(
                99L,
                20,
                5
        );

        mvc.perform(put("/api/products/3/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El productId del body no coincide con el id de la ruta"));
    }

    @Test
    void updateInventory_shouldReturn400WhenRequestIsInvalid() throws Exception {
        var req = new UpdateInventoryRequest(
                3L,
                -1,
                -5
        );

        mvc.perform(put("/api/products/3/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void changeStatus_shouldReturn200() throws Exception {
        var resp = new ProductResponse(
                3L,
                "Libro",
                "SKU-300",
                new BigDecimal("40000"),
                false,
                1L
        );

        when(productService.changeProductStatus(3L, false)).thenReturn(resp);

        mvc.perform(patch("/api/products/3/status")
                        .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void changeStatus_shouldReturn404WhenNotFound() throws Exception {
        when(productService.changeProductStatus(99L, false))
                .thenThrow(new ResourceNotFoundException("Producto no encontrado"));

        mvc.perform(patch("/api/products/99/status")
                        .param("active", "false"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Producto no encontrado"));
    }
}
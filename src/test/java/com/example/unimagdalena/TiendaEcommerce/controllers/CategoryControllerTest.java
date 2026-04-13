package com.example.unimagdalena.TiendaEcommerce.controllers;

import com.example.unimagdalena.TiendaEcommerce.dto.CategoryDto.CategoryResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.CategoryDto.CreateCategoryRequest;
import com.example.unimagdalena.TiendaEcommerce.dto.CategoryDto.UpdateCategoryRequest;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.services.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    MockMvc mvc;

    private final ObjectMapper om = new ObjectMapper();

    @MockitoBean
    CategoryService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new CreateCategoryRequest("Tecnologia");
        var resp = new CategoryResponse(10L, "Tecnologia");

        when(service.createCategory(any(CreateCategoryRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.endsWith("/api/categories/10")))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Tecnologia"));
    }

    @Test
    void create_shouldReturn400WhenNameIsBlank() throws Exception {
        var req = new CreateCategoryRequest("");

        mvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.violations[0].field").value("name"));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(service.getCategoryById(5L)).thenReturn(new CategoryResponse(5L, "Libros"));

        mvc.perform(get("/api/categories/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Libros"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(service.getCategoryById(99L))
                .thenThrow(new ResourceNotFoundException("Categoría no encontrada"));

        mvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Categoría no encontrada"));
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        when(service.getAllCategories()).thenReturn(List.of(
                new CategoryResponse(1L, "Libros"),
                new CategoryResponse(2L, "Accesorios")
        ));

        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Libros"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Accesorios"));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new UpdateCategoryRequest("Papeleria");
        var resp = new CategoryResponse(3L, "Papeleria");

        when(service.updateCategory(3L, req)).thenReturn(resp);

        mvc.perform(put("/api/categories/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Papeleria"));
    }

    @Test
    void update_shouldReturn404WhenCategoryDoesNotExist() throws Exception {
        var req = new UpdateCategoryRequest("Papeleria");

        when(service.updateCategory(50L, req))
                .thenThrow(new ResourceNotFoundException("Categoría no encontrada"));

        mvc.perform(put("/api/categories/50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Categoría no encontrada"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/categories/3"))
                .andExpect(status().isNoContent());

        verify(service).deleteCategory(3L);
    }
}
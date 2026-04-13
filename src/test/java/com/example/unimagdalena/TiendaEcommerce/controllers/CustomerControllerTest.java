package com.example.unimagdalena.TiendaEcommerce.controllers;

import com.example.unimagdalena.TiendaEcommerce.dto.CustomerDto.CreateCustomerRequest;
import com.example.unimagdalena.TiendaEcommerce.dto.CustomerDto.CustomerResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.CustomerDto.UpdateCustomerRequest;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.services.CustomerService;
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

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    MockMvc mvc;

    private final ObjectMapper om = new ObjectMapper();

    @MockitoBean
    CustomerService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new CreateCustomerRequest(
                "Ana",
                "Lopez",
                "ana@correo.com",
                "3001234567"
        );

        var resp = new CustomerResponse(
                10L,
                "Ana",
                "Lopez",
                "ana@correo.com",
                "3001234567",
                "ACTIVE"
        );

        when(service.createCustomer(any(CreateCustomerRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.endsWith("/api/customers/10")))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("Lopez"))
                .andExpect(jsonPath("$.email").value("ana@correo.com"))
                .andExpect(jsonPath("$.phoneNumber").value("3001234567"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void create_shouldReturn400WhenRequestIsInvalid() throws Exception {
        var req = new CreateCustomerRequest(
                "",
                "",
                "correo-malo",
                ""
        );

        mvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(service.getCustomerById(5L)).thenReturn(new CustomerResponse(
                5L,
                "Juan",
                "Perez",
                "juan@correo.com",
                "3001111111",
                "ACTIVE"
        ));

        mvc.perform(get("/api/customers/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Perez"))
                .andExpect(jsonPath("$.email").value("juan@correo.com"))
                .andExpect(jsonPath("$.phoneNumber").value("3001111111"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(service.getCustomerById(99L))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado"));

        mvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"));
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        when(service.getAllCustomers()).thenReturn(List.of(
                new CustomerResponse(1L, "Ana", "Lopez", "ana@correo.com", "3001234567", "ACTIVE"),
                new CustomerResponse(2L, "Luis", "Martinez", "luis@correo.com", "3007654321", "ACTIVE")
        ));

        mvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Ana"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].firstName").value("Luis"));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new UpdateCustomerRequest(
                "Carlos",
                "Gomez",
                "carlos@correo.com",
                "3002222222"
        );

        var resp = new CustomerResponse(
                3L,
                "Carlos",
                "Gomez",
                "carlos@correo.com",
                "3002222222",
                "ACTIVE"
        );

        when(service.updateCustomer(3L, req)).thenReturn(resp);

        mvc.perform(put("/api/customers/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.firstName").value("Carlos"))
                .andExpect(jsonPath("$.lastName").value("Gomez"))
                .andExpect(jsonPath("$.email").value("carlos@correo.com"))
                .andExpect(jsonPath("$.phoneNumber").value("3002222222"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void update_shouldReturn400WhenRequestIsInvalid() throws Exception {
        var req = new UpdateCustomerRequest(
                "",
                "",
                "correo-malo",
                ""
        );

        mvc.perform(put("/api/customers/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void update_shouldReturn404WhenCustomerDoesNotExist() throws Exception {
        var req = new UpdateCustomerRequest(
                "Carlos",
                "Gomez",
                "carlos@correo.com",
                "3002222222"
        );

        when(service.updateCustomer(50L, req))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado"));

        mvc.perform(put("/api/customers/50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/customers/3"))
                .andExpect(status().isNoContent());

        verify(service).deleteCustomer(3L);
    }
}
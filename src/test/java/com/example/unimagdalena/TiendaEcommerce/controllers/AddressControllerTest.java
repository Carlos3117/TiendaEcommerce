package com.example.unimagdalena.TiendaEcommerce.controllers;

import com.example.unimagdalena.TiendaEcommerce.dto.AddressDto.AddressResponse;
import com.example.unimagdalena.TiendaEcommerce.dto.AddressDto.CreateAddressRequest;
import com.example.unimagdalena.TiendaEcommerce.dto.AddressDto.UpdateAddressRequest;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.services.AddressService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired
    MockMvc mvc;

    private final ObjectMapper om = new ObjectMapper();

    @MockitoBean
    AddressService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new CreateAddressRequest(
                "Calle 123",
                "Santa Marta",
                5L
        );

        var resp = new AddressResponse(
                10L,
                "Calle 123",
                "Santa Marta",
                5L
        );

        when(service.createAddress(any(CreateAddressRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/customers/5/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.endsWith("/api/addresses/10")))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.street").value("Calle 123"))
                .andExpect(jsonPath("$.city").value("Santa Marta"))
                .andExpect(jsonPath("$.customerId").value(5));
    }

    @Test
    void create_shouldReturn400WhenCustomerIdFromBodyDoesNotMatchPath() throws Exception {
        var req = new CreateAddressRequest(
                "Calle 123",
                "Santa Marta",
                99L
        );

        mvc.perform(post("/api/customers/5/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El customerId del body no coincide con el customerId de la ruta"));
    }

    @Test
    void create_shouldReturn400WhenRequestIsInvalid() throws Exception {
        var req = new CreateAddressRequest(
                "",
                "",
                null
        );

        mvc.perform(post("/api/customers/5/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void getByCustomer_shouldReturn200() throws Exception {
        when(service.getAddressesByCustomer(5L)).thenReturn(List.of(
                new AddressResponse(1L, "Calle 1", "Santa Marta", 5L),
                new AddressResponse(2L, "Calle 2", "Santa Marta", 5L)
        ));

        mvc.perform(get("/api/customers/5/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].street").value("Calle 1"))
                .andExpect(jsonPath("$[0].city").value("Santa Marta"))
                .andExpect(jsonPath("$[0].customerId").value(5))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].street").value("Calle 2"));
    }

    @Test
    void getByCustomer_shouldReturn404WhenCustomerDoesNotExist() throws Exception {
        when(service.getAddressesByCustomer(99L))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado"));

        mvc.perform(get("/api/customers/99/addresses"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(service.getAddressById(3L))
                .thenReturn(new AddressResponse(3L, "Calle 50", "Barranquilla", 7L));

        mvc.perform(get("/api/addresses/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.street").value("Calle 50"))
                .andExpect(jsonPath("$.city").value("Barranquilla"))
                .andExpect(jsonPath("$.customerId").value(7));
    }

    @Test
    void getById_shouldReturn404WhenAddressDoesNotExist() throws Exception {
        when(service.getAddressById(99L))
                .thenThrow(new ResourceNotFoundException("Dirección no encontrada"));

        mvc.perform(get("/api/addresses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Dirección no encontrada"));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new UpdateAddressRequest(
                "Nueva Calle 10",
                "Bogota"
        );

        var resp = new AddressResponse(
                4L,
                "Nueva Calle 10",
                "Bogota",
                2L
        );

        when(service.updateAddress(4L, req)).thenReturn(resp);

        mvc.perform(put("/api/addresses/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.street").value("Nueva Calle 10"))
                .andExpect(jsonPath("$.city").value("Bogota"))
                .andExpect(jsonPath("$.customerId").value(2));
    }

    @Test
    void update_shouldReturn404WhenAddressDoesNotExist() throws Exception {
        var req = new UpdateAddressRequest(
                "Nueva Calle 10",
                "Bogota"
        );

        when(service.updateAddress(99L, req))
                .thenThrow(new ResourceNotFoundException("Dirección no encontrada"));

        mvc.perform(put("/api/addresses/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Dirección no encontrada"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/addresses/3"))
                .andExpect(status().isNoContent());

        verify(service).deleteAddress(3L);
    }

    @Test
    void delete_shouldReturn404WhenAddressDoesNotExist() throws Exception {
        doThrow(new ResourceNotFoundException("Dirección no encontrada"))
                .when(service).deleteAddress(99L);

        mvc.perform(delete("/api/addresses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Dirección no encontrada"));
    }
}
package com.example.store.controller;

import com.example.store.dto.OrderCreateDTO;
import com.example.store.dto.OrderDTO;
import com.example.store.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setDescription("Test Order");
        orderDTO.setProducts(List.of());
    }

    // ---------------------- GET /order ----------------------

    @Test
    @DisplayName("Should return all orders successfully")
    void shouldReturnAllOrders() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(orderDTO));

        mockMvc.perform(get("/order/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Test Order"))
                .andExpect(jsonPath("$[0].products").isArray());
    }

    @Test
    @DisplayName("Should return empty list when no orders found")
    void shouldReturnEmptyListWhenNoOrdersFound() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of());

        mockMvc.perform(get("/order/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ---------------------- GET /order/{id} ----------------------

    @Test
    @DisplayName("Should return specific order by ID successfully")
    void shouldReturnOrderById() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(orderDTO);

        mockMvc.perform(get("/order/find/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test Order"));
    }

    @Test
    @DisplayName("Should return 404 when order not found")
    void shouldReturnNotFoundWhenOrderMissing() throws Exception {
        when(orderService.getOrderById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        mockMvc.perform(get("/order/find/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order not found"));
    }

    @Test
    @DisplayName("Should return 400 when invalid path variable type")
    void shouldReturnBadRequestForInvalidPathVariable() throws Exception {
        mockMvc.perform(get("/order/find/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }

    // ---------------------- POST /order ----------------------

    @Test
    @DisplayName("Should create a new order successfully")
    void shouldCreateOrder() throws Exception {
        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setDescription("Laptop purchase");
        createDTO.setCustomerId(1L);
        createDTO.setProductIds(List.of(10L, 11L));

        when(orderService.createOrder(any(OrderCreateDTO.class))).thenReturn(orderDTO);

        mockMvc.perform(post("/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test Order"));
    }

    @Test
    @DisplayName("Should return 400 when creating order with missing description")
    void shouldReturnBadRequestWhenDescriptionMissing() throws Exception {
        OrderCreateDTO invalidDTO = new OrderCreateDTO();
        invalidDTO.setDescription(""); // invalid due to @NotBlank
        invalidDTO.setCustomerId(1L);
        invalidDTO.setProductIds(List.of(10L));

        mockMvc.perform(post("/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("description"))));
    }

    @Test
    @DisplayName("Should return 400 when creating order with null customer ID")
    void shouldReturnBadRequestWhenCustomerIdNull() throws Exception {
        OrderCreateDTO invalidDTO = new OrderCreateDTO();
        invalidDTO.setDescription("Valid description");
        invalidDTO.setCustomerId(null); // invalid @NotNull
        invalidDTO.setProductIds(List.of(1L));

        mockMvc.perform(post("/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("Customer ID"))));
    }

    @Test
    @DisplayName("Should return 500 when service layer throws unexpected exception")
    void shouldReturnInternalServerErrorWhenServiceFails() throws Exception {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setDescription("Valid Order");
        dto.setCustomerId(1L);

        when(orderService.createOrder(any(OrderCreateDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}

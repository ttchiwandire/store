package com.example.store.controller;

import com.example.store.dto.CustomerCreateDTO;
import com.example.store.dto.CustomerDTO;
import com.example.store.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerDTO customerDTO;

    @BeforeEach
    void setup() {
        customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setName("Tatenda");
    }

    // --------------------- GET /customer (list all) ----------------------

    @Test
    @DisplayName("Should return all customers successfully")
    void shouldReturnAllCustomers() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of(customerDTO));

        mockMvc.perform(get("/customer/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Tatenda"));
    }

    // --------------------- GET /customer?page&size ----------------------

    @Test
    @DisplayName("Should return paginated customers")
    void shouldReturnPaginatedCustomers() throws Exception {
        Page<CustomerDTO> page = new PageImpl<>(List.of(customerDTO), PageRequest.of(0, 20), 1);
        when(customerService.getAllCustomers(eq(0), eq(20))).thenReturn(page);

        mockMvc.perform(get("/customer/list/paged")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Tatenda"));
    }

    // --------------------- GET /customer/search?query=... ----------------------

    @Test
    @DisplayName("Should return search results successfully")
    void shouldReturnSearchResults() throws Exception {
        when(customerService.searchCustomers("tate")).thenReturn(List.of(customerDTO));

        mockMvc.perform(get("/customer/search").param("query", "tate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tatenda"));
    }

    @Test
    @DisplayName("Should return empty list when search yields no results")
    void shouldReturnEmptyListOnSearchNoResults() throws Exception {
        when(customerService.searchCustomers("unknown")).thenReturn(List.of());

        mockMvc.perform(get("/customer/search").param("query", "unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // --------------------- POST /customer ----------------------

    @Test
    @DisplayName("Should create a new customer successfully")
    void shouldCreateCustomer() throws Exception {
        CustomerCreateDTO createDTO = new CustomerCreateDTO();
        createDTO.setName("Alice");

        when(customerService.createCustomer(any(CustomerCreateDTO.class))).thenReturn(customerDTO);

        mockMvc.perform(post("/customer/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Tatenda"));
    }

    @Test
    @DisplayName("Should return 400 when creating customer with blank name")
    void shouldReturnBadRequestWhenNameBlank() throws Exception {
        CustomerCreateDTO invalid = new CustomerCreateDTO();
        invalid.setName(""); // invalid due to @NotBlank

        mockMvc.perform(post("/customer/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(greaterThanOrEqualTo(1))));
    }

    // --------------------- Error / Exception Handling ----------------------

    @Test
    @DisplayName("Should return 404 when service throws not found exception")
    void shouldReturnNotFoundWhenServiceThrows() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Customer not found"))
                .when(customerService).getAllCustomers(anyInt(), anyInt());

        mockMvc.perform(get("/customer/list/paged"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }
}

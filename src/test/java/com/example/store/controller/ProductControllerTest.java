package com.example.store.controller;

import com.example.store.dto.ProductCreateDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.service.ProductService;
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
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprehensive tests for ProductController
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDTO productDTO;

    @BeforeEach
    void setup() {
        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setDescription("Laptop");
    }

    // --------------------------------------------------------
    // GET /products
    // --------------------------------------------------------

    @Test
    @DisplayName("Should return all products successfully")
    void shouldReturnAllProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(productDTO));

        mockMvc.perform(get("/products/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Laptop"));
    }

    @Test
    @DisplayName("Should return empty list when no products found")
    void shouldReturnEmptyListWhenNoProductsFound() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of());

        mockMvc.perform(get("/products/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // --------------------------------------------------------
    // GET /products/{id}
    // --------------------------------------------------------

    @Test
    @DisplayName("Should return product by ID successfully")
    void shouldReturnProductById() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productDTO);

        mockMvc.perform(get("/products/find/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Laptop"));
    }

    @Test
    @DisplayName("Should return 404 when product not found")
    void shouldReturnNotFoundWhenProductMissing() throws Exception {
        when(productService.getProductById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        mockMvc.perform(get("/products/find/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    @DisplayName("Should return 400 when invalid path variable type")
    void shouldReturnBadRequestForInvalidPathVariable() throws Exception {
        mockMvc.perform(get("/products/find/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid value")))
                .andExpect(jsonPath("$.path").value("/products/find/abc"));
    }

    // --------------------------------------------------------
    // POST /products
    // --------------------------------------------------------

    @Test
    @DisplayName("Should create new product successfully")
    void shouldCreateProduct() throws Exception {
        ProductCreateDTO createDTO = new ProductCreateDTO();
        createDTO.setDescription("Smartphone");

        when(productService.createProduct(any(ProductCreateDTO.class))).thenReturn(productDTO);

        mockMvc.perform(post("/products/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Laptop")); // from mock
    }

    @Test
    @DisplayName("Should return 400 when product description is blank")
    void shouldReturnBadRequestWhenDescriptionBlank() throws Exception {
        ProductCreateDTO invalid = new ProductCreateDTO();
        invalid.setDescription(""); // invalid due to @NotBlank

        mockMvc.perform(post("/products/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("description"))));
    }

    @Test
    @DisplayName("Should return 400 when product description is missing")
    void shouldReturnBadRequestWhenDescriptionMissing() throws Exception {
        String invalidJson = "{}"; // missing description field

        mockMvc.perform(post("/products/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("Should return 500 when service throws unexpected error")
    void shouldReturnInternalServerErrorWhenServiceFails() throws Exception {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setDescription("Tablet");

        when(productService.createProduct(any(ProductCreateDTO.class)))
                .thenThrow(new RuntimeException("Unexpected database error"));

        mockMvc.perform(post("/products/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}

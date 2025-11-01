package com.example.store.service;

import com.example.store.dto.ProductCreateDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private ProductMapper productMapper;
    private ProductService productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setup() {
        productRepository = mock(ProductRepository.class);
        productMapper = mock(ProductMapper.class);
        productService = new ProductService(productRepository, productMapper);

        product = new Product();
        product.setId(1L);
        product.setDescription("Laptop");

        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setDescription("Laptop");
    }

    // ----------------------------------------------------------
    // createProduct()
    // ----------------------------------------------------------
    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setDescription("Monitor");

        Product mapped = new Product();
        mapped.setDescription("Monitor");

        given(productMapper.productCreateDtoToProduct(dto)).willReturn(mapped);
        given(productRepository.save(any(Product.class))).willReturn(product);
        given(productMapper.productToProductDTO(product)).willReturn(productDTO);

        ProductDTO result = productService.createProduct(dto);

        assertThat(result.getDescription()).isEqualTo("Laptop");
        verify(productMapper).productCreateDtoToProduct(dto);
        verify(productRepository).save(any(Product.class));
        verify(productMapper).productToProductDTO(product);
    }

    @Test
    @DisplayName("Should propagate repository save result correctly")
    void shouldPropagateRepositoryResult() {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setDescription("Keyboard");

        Product mapped = new Product();
        mapped.setDescription("Keyboard");

        given(productMapper.productCreateDtoToProduct(dto)).willReturn(mapped);
        given(productRepository.save(mapped)).willReturn(product);
        given(productMapper.productToProductDTO(product)).willReturn(productDTO);

        ProductDTO result = productService.createProduct(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Laptop");
    }

    // ----------------------------------------------------------
    // getAllProducts()
    // ----------------------------------------------------------
    @Test
    @DisplayName("Should return all products successfully")
    void shouldReturnAllProducts() {
        given(productRepository.findAll()).willReturn(List.of(product));
        given(productMapper.productsToProductDTOs(List.of(product))).willReturn(List.of(productDTO));

        List<ProductDTO> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Laptop");
        verify(productRepository).findAll();
        verify(productMapper).productsToProductDTOs(anyList());
    }

    @Test
    @DisplayName("Should return empty list when no products exist")
    void shouldReturnEmptyListWhenNoProductsExist() {
        given(productRepository.findAll()).willReturn(List.of());
        given(productMapper.productsToProductDTOs(List.of())).willReturn(List.of());

        List<ProductDTO> result = productService.getAllProducts();

        assertThat(result).isEmpty();
        verify(productRepository).findAll();
        verify(productMapper).productsToProductDTOs(anyList());
    }

    // ----------------------------------------------------------
    // getProductById()
    // ----------------------------------------------------------
    @Test
    @DisplayName("Should return product by ID successfully")
    void shouldReturnProductById() {
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(productMapper.productToProductDTO(product)).willReturn(productDTO);

        ProductDTO result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Laptop");
        verify(productRepository).findById(1L);
        verify(productMapper).productToProductDTO(product);
    }

    @Test
    @DisplayName("Should throw 404 when product not found")
    void shouldThrowNotFoundWhenProductMissing() {
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        ResponseStatusException ex = catchThrowableOfType(
                () -> productService.getProductById(999L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).isEqualTo("Product not found");
        verify(productRepository).findById(999L);
        verify(productMapper, never()).productToProductDTO(any());
    }

    // ----------------------------------------------------------
    // Logging / Defensive calls
    // ----------------------------------------------------------
    @Test
    @DisplayName("Should log and call repository during normal execution")
    void shouldLogAndCallRepository() {
        given(productRepository.findAll()).willReturn(List.of(product));
        given(productMapper.productsToProductDTOs(List.of(product))).willReturn(List.of(productDTO));

        List<ProductDTO> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        verify(productRepository).findAll();
    }
}

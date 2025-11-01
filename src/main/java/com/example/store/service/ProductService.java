package com.example.store.service;

import com.example.store.dto.ProductCreateDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductDTO createProduct(ProductCreateDTO dto) {
        log.info("Creating product: {}", dto.getDescription());
        Product product = productMapper.productCreateDtoToProduct(dto);
        Product saved = productRepository.save(product);
        log.info("Product created with id={}", saved.getId());
        return productMapper.productToProductDTO(saved);
    }

    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products");
        List<ProductDTO> products = productMapper.productsToProductDTOs(productRepository.findAll());
        log.debug("Fetched {} products", products.size());
        return products;
    }

    public ProductDTO getProductById(Long id) {
        log.info("Fetching product by id={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
                });
        return productMapper.productToProductDTO(product);
    }
}
package com.example.store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductCreateDTO {
    @NotBlank(message = "Product description is required")
    private String description;
}
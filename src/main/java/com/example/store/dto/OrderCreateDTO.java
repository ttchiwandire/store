package com.example.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {
    @NotBlank(message = "Order description is required")
    private String description;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private List<Long> productIds;
}

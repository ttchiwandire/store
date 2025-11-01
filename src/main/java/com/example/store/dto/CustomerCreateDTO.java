package com.example.store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerCreateDTO {
    @NotBlank(message = "Customer name is required")
    private String name;
}

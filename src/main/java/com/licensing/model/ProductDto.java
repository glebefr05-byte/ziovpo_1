package com.licensing.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.UUID;

@Data
public class ProductDto {
    private UUID id;

    @NotBlank(message = "Product name is required")
    private String name;

    private Boolean isBlocked = false;
}
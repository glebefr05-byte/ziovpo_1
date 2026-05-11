package com.licensing.controller.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.UUID;

@Data
public class CreateLicenseRequest {
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "License type ID is required")
    private UUID typeId;

    @NotNull(message = "Owner ID is required")
    private UUID ownerId;

    @Min(value = 1, message = "Device count must be at least 1")
    private Integer deviceCount = 1;

    private String description;
}
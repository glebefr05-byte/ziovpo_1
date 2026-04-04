package com.licensing.controller.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class CheckLicenseRequest {
    @NotBlank(message = "Device MAC address is required")
    private String deviceMac;

    @NotNull(message = "Product ID is required")
    private UUID productId;
}
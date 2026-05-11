package com.licensing.controller.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class RenewLicenseRequest {
    @NotBlank(message = "Activation key is required")
    private String activationKey;
}
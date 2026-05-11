package com.licensing.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.UUID;

@Data
public class DeviceDto {
    private UUID id;

    @NotBlank(message = "Device name is required")
    private String name;

    @NotBlank(message = "MAC address is required")
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$",
            message = "Invalid MAC address format")
    private String macAddress;

    @NotNull(message = "User ID is required")
    private UUID userId;
}
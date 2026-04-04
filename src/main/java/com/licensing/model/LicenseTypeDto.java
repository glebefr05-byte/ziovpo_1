package com.licensing.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.util.UUID;

@Data
public class LicenseTypeDto {
    private UUID id;

    @NotBlank(message = "License type name is required")
    private String name;

    @NotNull(message = "Default duration is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer defaultDurationInDays;

    private String description;
}
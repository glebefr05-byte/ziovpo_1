package com.licensing.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SignaturesByIdsRequest {
    @NotNull(message = "IDs list is required")
    private List<UUID> ids;
}
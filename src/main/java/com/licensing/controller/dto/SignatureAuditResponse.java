package com.licensing.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SignatureAuditResponse {
    private Long auditId;
    private UUID signatureId;
    private String changedBy;
    private Instant changedAt;
    private String fieldsChanged;
    private String description;
}
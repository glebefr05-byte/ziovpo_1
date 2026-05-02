package com.licensing.controller.dto;

import com.licensing.model.enums.SignatureStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SignatureHistoryResponse {
    private Long historyId;
    private UUID signatureId;
    private Instant versionCreatedAt;
    private String threatName;
    private String firstBytesHex;
    private String remainderHashHex;
    private Long remainderLength;
    private String fileType;
    private Long offsetStart;
    private Long offsetEnd;
    private Instant updatedAt;
    private SignatureStatus status;
    private String digitalSignatureBase64;
}
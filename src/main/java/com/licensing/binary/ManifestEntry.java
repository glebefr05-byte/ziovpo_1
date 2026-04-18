package com.licensing.binary;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ManifestEntry {
    private UUID id;
    private byte statusCode;
    private long updatedAtEpochMillis;
    private long dataOffset;
    private int dataLength;
    private byte[] recordSignatureBytes;
}
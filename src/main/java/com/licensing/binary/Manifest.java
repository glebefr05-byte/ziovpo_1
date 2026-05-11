package com.licensing.binary;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class Manifest {
    private String magic;
    private short version;
    private byte exportType;
    private long generatedAtEpochMillis;
    private long sinceEpochMillis;
    private int recordCount;
    private byte[] dataSha256;

    private ManifestEntry[] entries;

    private byte[] manifestSignature;
}
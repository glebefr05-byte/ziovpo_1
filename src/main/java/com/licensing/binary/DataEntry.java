package com.licensing.binary;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataEntry {
    private String threatName;
    private byte[] firstBytes;
    private byte[] remainderHash;
    private long remainderLength;
    private String fileType;
    private long offsetStart;
    private long offsetEnd;
}
package com.licensing.signature;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignatureResult {
    private Object payload;
    private String signature;
    private String algorithm;
    private String certificateHash;
}
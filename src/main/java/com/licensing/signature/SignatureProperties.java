package com.licensing.signature;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "signature")
public class SignatureProperties {
    @Value("&{signature.key-store-path}")
    private String keyStorePath;

    @Value("&{signature.key-store-type}")
    private String keyStoreType;

    @Value("&{signature.key-store-password}")
    private String keyStorePassword;

    @Value("&{signature.key-store-alias}")
    private String keyAlias;

    @Value("&{signature.key-store-password}")
    private String keyPassword;

    @Value("&{signature.algorithm}")
    private String algorithm;

    private boolean cacheKeys = true;
}
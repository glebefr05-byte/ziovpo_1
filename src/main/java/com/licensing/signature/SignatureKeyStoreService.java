package com.licensing.signature;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignatureKeyStoreService {

    private final SignatureKeyProvider keyProvider;
    private final JsonCanonicalizer canonicalizer;
    private final SignatureProperties properties;

    public String sign(Object payload) {
        try {
            byte[] canonicalBytes = canonicalizer.canonizeJson(payload).getBytes(StandardCharsets.UTF_8);
            return signBytes(canonicalBytes);
        } catch (Exception e) {
            log.error("Failed to sign payload", e);
            throw new RuntimeException("Signing failed: " + e.getMessage(), e);
        }
    }

    public String signBytes(byte[] data) {
        try {
            Signature signature = Signature.getInstance(properties.getAlgorithm());
            signature.initSign(keyProvider.getPrivateKey());
            signature.update(data);
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("Failed to sign bytes", e);
            throw new RuntimeException("Signing failed: " + e.getMessage(), e);
        }
    }

    public byte[] signBytesToBytes(byte[] data) {
        try {
            Signature signature = Signature.getInstance(properties.getAlgorithm());
            signature.initSign(keyProvider.getPrivateKey());
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            log.error("Failed to sign bytes", e);
            throw new RuntimeException("Signing failed: " + e.getMessage(), e);
        }
    }

    public boolean verifyBytes(byte[] data, byte[] signatureBytes) {
        try {
            Signature signature = Signature.getInstance(properties.getAlgorithm());
            signature.initVerify(keyProvider.getPublicKey());
            signature.update(data);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            log.error("Failed to verify signature", e);
            return false;
        }
    }

    public boolean verify(Object payload, String signatureBase64) {
        try {
            byte[] canonicalBytes = canonicalizer.canonizeJson(payload).getBytes(StandardCharsets.UTF_8);

            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            Signature signature = Signature.getInstance(properties.getAlgorithm());
            signature.initVerify(keyProvider.getPublicKey());
            signature.update(canonicalBytes);
            boolean isValid = signature.verify(signatureBytes);

            log.info("Signature verification result: {}", isValid);
            return isValid;

        } catch (Exception e) {
            log.error("Failed to verify signature", e);
            return false;
        }
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyProvider.getPublicKey().getEncoded());
    }

    public String getAlgorithm() {
        return properties.getAlgorithm();
    }

    public String getCertificateHash() {
        try {
            X509Certificate certificate = keyProvider.getCertificate();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] thumbprint = md.digest(certificate.getEncoded());
            return bytesToHex(thumbprint);
        } catch (Exception e) {
            log.error("Failed to get certificate thumbprint", e);
            return "UNKNOWN";
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
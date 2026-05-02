package com.licensing.signature;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Signature;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignatureKeyStoreService {

    private final SignatureKeyProvider keyProvider;
    private final JsonCanonicalizer canonicalizer;
    private final SignatureProperties properties;

    public String sign(Object payload) {
        try {
            byte[] canonicalBytes = canonicalizer.canonizeJson(payload).getBytes();

            if (log.isDebugEnabled()) {
                log.debug("Canonical JSON: {}", new String(canonicalBytes));
                log.debug("Canonical bytes length: {}", canonicalBytes.length);
            }

            Signature signature = Signature.getInstance(properties.getAlgorithm());
            signature.initSign(keyProvider.getPrivateKey());
            signature.update(canonicalBytes);
            byte[] signatureBytes = signature.sign();

            String base64Signature = Base64.getEncoder().encodeToString(signatureBytes);

            log.info("Successfully signed payload, signature length: {}", base64Signature.length());
            return base64Signature;

        } catch (Exception e) {
            log.error("Failed to sign payload", e);
            throw new RuntimeException("Signing failed: " + e.getMessage(), e);
        }
    }

    public boolean verify(Object payload, String signatureBase64) {
        try {
            byte[] canonicalBytes = canonicalizer.canonizeJson(payload).getBytes();

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

    public SignatureResult signWithResult(Object payload) {
        String signature = sign(payload);
        return SignatureResult.builder()
                .payload(payload)
                .signature(signature)
                .algorithm(properties.getAlgorithm())
                .build();
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
            byte[] hash = md.digest(certificate.getEncoded());
            return bytesToHex(hash);
        } catch (Exception e) {
            log.error("Failed to get certificate hash", e);
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
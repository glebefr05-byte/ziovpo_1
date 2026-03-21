package com.licensing.signature;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Component
public class SignatureKeyProvider {

    private final SignatureProperties properties;
    private final ResourceLoader resourceLoader;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private X509Certificate certificate;

    public SignatureKeyProvider(SignatureProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        loadKeys();
    }

    private void loadKeys() {
        lock.writeLock().lock();
        try {
            log.info("Loading signing keys from: {}", properties.getKeyStorePath());

            KeyStore keyStore = KeyStore.getInstance(properties.getKeyStoreType());
            Resource resource = resourceLoader.getResource(properties.getKeyStorePath());

            try (InputStream is = resource.getInputStream()) {
                char[] storePassword = properties.getKeyStorePassword().toCharArray();
                keyStore.load(is, storePassword);

                char[] keyPassword = StringUtils.hasText(properties.getKeyPassword())
                        ? properties.getKeyPassword().toCharArray()
                        : storePassword;

                Key key = keyStore.getKey(properties.getKeyAlias(), keyPassword);
                if (key instanceof PrivateKey) {
                    privateKey = (PrivateKey) key;
                    log.info("Private key loaded successfully");
                } else {
                    throw new IllegalStateException("Key is not a private key");
                }

                Certificate cert = keyStore.getCertificate(properties.getKeyAlias());
                if (cert instanceof X509Certificate) {
                    certificate = (X509Certificate) cert;
                    publicKey = certificate.getPublicKey();
                    log.info("Certificate loaded successfully");
                    log.info("Certificate subject: {}", certificate.getSubjectDN());
                    log.info("Certificate expiry: {}", certificate.getNotAfter());
                } else {
                    throw new IllegalStateException("Certificate is not X.509");
                }

            } catch (IOException e) {
                throw new RuntimeException("Failed to read keystore file: " + properties.getKeyStorePath(), e);
            }

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException e) {
            throw new RuntimeException("Failed to load signing keys", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public PrivateKey getPrivateKey() {
        lock.readLock().lock();
        try {
            if (privateKey == null) {
                throw new IllegalStateException("Private key not loaded");
            }
            return privateKey;
        } finally {
            lock.readLock().unlock();
        }
    }

    public PublicKey getPublicKey() {
        lock.readLock().lock();
        try {
            if (publicKey == null) {
                throw new IllegalStateException("Public key not loaded");
            }
            return publicKey;
        } finally {
            lock.readLock().unlock();
        }
    }

    public X509Certificate getCertificate() {
        lock.readLock().lock();
        try {
            if (certificate == null) {
                throw new IllegalStateException("Certificate not loaded");
            }
            return certificate;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(getPublicKey().getEncoded());
    }

    public void reloadKeys() {
        loadKeys();
    }
}
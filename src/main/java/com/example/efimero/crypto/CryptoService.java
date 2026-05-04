package com.example.efimero.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Arrays;

@Component
public class CryptoService {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LEN = 12;

    private final SecretKeySpec keySpec;
    private final SecureRandom random = new SecureRandom();

    public CryptoService(@Value("${app.encryption.key}") String configuredKey) {
        if (configuredKey == null || configuredKey.isBlank()) {
            throw new IllegalStateException("app.encryption.key must be provided via environment variable.");
        }
        byte[] keyBytes = tryBase64(configuredKey);
        if (keyBytes == null)
            keyBytes = configuredKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) {
            throw new IllegalStateException("Encryption key must be 32 bytes.");
        }

        this.keySpec = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plain) {
        if (plain == null)
            return null;
        try {
            byte[] iv = new byte[IV_LEN];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] blob = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, blob, 0, iv.length);
            System.arraycopy(ct, 0, blob, iv.length, ct.length);
            return Base64.getEncoder().encodeToString(blob);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public String decrypt(String blobB64) {
        if (blobB64 == null)
            return null;
        try {
            byte[] blob = Base64.getDecoder().decode(blobB64);
            byte[] iv = Arrays.copyOfRange(blob, 0, IV_LEN);
            byte[] ct = Arrays.copyOfRange(blob, IV_LEN, blob.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] pt = cipher.doFinal(ct);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    private byte[] tryBase64(String v) {
        try {
            return Base64.getDecoder().decode(v);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
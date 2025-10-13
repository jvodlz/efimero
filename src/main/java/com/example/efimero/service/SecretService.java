package com.example.efimero.service;

import com.example.efimero.crypto.CryptoService;
import com.example.efimero.domain.Secret;
import com.example.efimero.repo.SecretRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class SecretService {
    private final SecretRepository repository;
    private final CryptoService crypto;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.secret.ttl-minutes}")
    private long ttlMinutes;

    public SecretService(SecretRepository repository, CryptoService crypto) {
        this.repository = repository;
        this.crypto = crypto;
    }

    public CreateSecret createSecret(String plaintext) {
        String key;
        String hashed;

        for (int i = 0; i < 3; i++) {
            key = generateKey();
            hashed = hashKey(key);
            if (repository.findByHashedKey(hashed).isEmpty()) {
                String ciphertext = crypto.encrypt(plaintext);
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expires = now.plusMinutes(ttlMinutes);
                Secret secret = repository.save(new Secret(ciphertext, hashed, now, expires));
                return new CreateSecret(key, secret.getExpiresAt());
            }
        }
        throw new IllegalStateException("Failed to generate unique key, try again.");
    }

    @Transactional
    public Optional<String> readAndDestroy(String key) {
        String hashed = hashKey(key);
        Optional<Secret> opt = repository.findByHashedKey(hashed);
        if (opt.isEmpty())
            return Optional.empty();

        Secret secret = opt.get();
        if (secret.getExpiresAt().isBefore(LocalDateTime.now())) {
            repository.delete(secret);
            return Optional.empty();
        }

        String plain = crypto.decrypt(secret.getCiphertext());
        repository.delete(secret);
        return Optional.of(plain);
    }

    private String generateKey() {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashKey(String key) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha.digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Hashing failed", e);
        }
    }

    public record CreateSecret(String key, LocalDateTime expiresAt) {
    }
}

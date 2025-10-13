package com.example.efimero.service;

import com.example.efimero.repo.SecretRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SecretCleanupJob {

    private final SecretRepository repository;

    public SecretCleanupJob(SecretRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedDelayString = "${app.secret.cleanup-interval-ms:60000}")
    public void purgeExpired() {
        repository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
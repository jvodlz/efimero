package com.example.efimero.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "efimero", indexes = @Index(name = "ix_hashed_key", columnList = "hashedKey", unique = true))
@Data
@NoArgsConstructor

public class Secret {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ciphertext", nullable = false, length = 4096)
    private String ciphertext;

    @Column(nullable = false, unique = true, length = 64)
    private String hashedKey;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public Secret(String ciphertext, String hashedKey, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.ciphertext = ciphertext;
        this.hashedKey = hashedKey;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
}

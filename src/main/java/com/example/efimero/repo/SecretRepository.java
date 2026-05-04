package com.example.efimero.repo;

import com.example.efimero.domain.Secret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SecretRepository extends JpaRepository<Secret, Long> {
    Optional<Secret> findByHashedKey(String hashedKey);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    Long deleteByExpiresAtBefore(LocalDateTime cutoff);
}

package com.example.efimero.api;

import com.example.efimero.api.dto.CreateSecretRequest;
import com.example.efimero.api.dto.CreateSecretResponse;
import com.example.efimero.service.SecretService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("api/efimero")
public class SecretController {

    private final SecretService service;

    public SecretController(SecretService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CreateSecretResponse> create(@Valid @RequestBody CreateSecretRequest req) {
        var created = service.createSecret(req.getText());

        String url = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{key}")
                .buildAndExpand(created.key())
                .toUriString();

        long expiresInSec = Duration.between(LocalDateTime.now(), created.expiresAt()).toSeconds();

        var body = new CreateSecretResponse(url, Math.max(0, expiresInSec));
        return ResponseEntity.created(URI.create(url)).body(body);
    }

    @GetMapping(value = "/{key}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> read(@PathVariable String key) {
        return service.readAndDestroy(key)
                .map(text -> ResponseEntity.ok()
                        .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate")
                        .header(HttpHeaders.PRAGMA, "no-cache")
                        .header(HttpHeaders.EXPIRES, "0")
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(text))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

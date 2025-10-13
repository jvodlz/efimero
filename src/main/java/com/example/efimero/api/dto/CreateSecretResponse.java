package com.example.efimero.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateSecretResponse {
    private String url;
    private long expiresInSec;
}

package com.example.efimero.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSecretRequest {
    @NotBlank(message = "text cannot be blank")
    @Size(max = 4000, message = "text too large")
    private String text;
}

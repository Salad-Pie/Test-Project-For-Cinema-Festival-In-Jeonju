package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank(message = "name is required.")
        String name,
        String email,
        @Pattern(regexp = "^[0-9+\\-]{8,20}$", message = "phoneNumber format is invalid.")
        String phoneNumber,
        String providerUserId
) {
}

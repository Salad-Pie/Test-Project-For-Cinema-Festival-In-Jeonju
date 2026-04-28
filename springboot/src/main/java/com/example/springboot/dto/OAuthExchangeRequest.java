package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuthExchangeRequest(
        @NotBlank(message = "code is required.")
        String code,
        @NotBlank(message = "state is required.")
        String state
) {
}

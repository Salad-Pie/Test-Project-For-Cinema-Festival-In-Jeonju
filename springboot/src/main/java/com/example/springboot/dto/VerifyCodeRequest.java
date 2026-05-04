package com.example.springboot.dto;

import jakarta.validation.constraints.Pattern;

public record VerifyCodeRequest(
        String token,
        @Pattern(regexp = "^[0-9]{6}$", message = "identifier code must be 6 digits.")
        String code
) {
}

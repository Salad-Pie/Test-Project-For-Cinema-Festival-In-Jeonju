package com.example.springboot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailCodeVerifyRequest(
        @NotBlank(message = "email is required.")
        @Email(message = "email format is invalid.")
        String email,
        @NotBlank(message = "code is required.")
        @Pattern(regexp = "^\\d{6}$", message = "code must be 6 digits.")
        String code
) {
}

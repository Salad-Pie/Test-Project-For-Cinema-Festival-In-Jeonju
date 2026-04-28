package com.example.springboot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SponsorshipApplicationRequest(
        @NotBlank(message = "name is required.")
        String name,
        @NotBlank(message = "phoneNumber is required.")
        @Pattern(regexp = "^[0-9\\-+() ]{8,20}$", message = "phoneNumber format is invalid.")
        String phoneNumber,
        @NotBlank(message = "bankAccount is required.")
        @Pattern(regexp = "^[0-9\\- ]{8,40}$", message = "bankAccount format is invalid.")
        String bankAccount,
        @Min(value = 1, message = "amount must be greater than 0.")
        Long amount,
        @NotBlank(message = "address is required.")
        String address
) {
}

package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EndingCreditsEntryRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6}")
        String code
) {
}

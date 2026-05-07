package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;

public record SignaturePreviewConfirmRequest(
        @NotBlank
        String previewToken
) {
}

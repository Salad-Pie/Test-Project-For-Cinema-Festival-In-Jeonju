package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;

public record SaveSignatureRequest(
        @NotBlank(message = "signatureDataUrl is required.")
        String signatureDataUrl,
        String recognizedText
) {
}

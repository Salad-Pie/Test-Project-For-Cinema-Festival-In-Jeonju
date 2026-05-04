package com.example.springboot.dto;

public record CertificateSampleRequest(
        String title,
        String name,
        String signatureText,
        String fontFamily,
        Integer nameX,
        Integer nameY,
        Integer signatureX,
        Integer signatureY
) {
}

package com.example.springboot.dto;

public record RecentSignatureResponse(
        String code,
        String originalName,
        String koreanName
) {
}

package com.example.springboot.dto;

import java.time.LocalDateTime;

public record AdminSignatureResponse(
        Long id,
        Long userId,
        String imageUrl,
        String originalName,
        String recognizedText,
        String englishName,
        String koreanName,
        String koreanMeaningText,
        String nameLanguage,
        String detectedLanguage,
        String nameConversionSource,
        String ocrStatus,
        Double ocrConfidence,
        LocalDateTime ocrProcessedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

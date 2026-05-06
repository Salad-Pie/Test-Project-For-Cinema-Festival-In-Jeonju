package com.example.springboot.dto;

public record EndingCreditsEntryResponse(
        String englishName,
        String koreanName,
        boolean hasEnglishName,
        boolean hasKoreanName,
        boolean hasSignature
) {
}

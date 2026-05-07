package com.example.springboot.dto;

import com.example.springboot.domain.NameConversionSource;
import com.example.springboot.domain.NameLanguage;
import com.example.springboot.domain.OcrStatus;
import com.example.springboot.domain.SignatureLanguage;

public record SignaturePreviewResponse(
        String previewToken,
        String recognizedText,
        String originalName,
        NameLanguage nameLanguage,
        String koreanText,
        String koreanMeaningText,
        String englishName,
        String koreanName,
        NameConversionSource nameConversionSource,
        SignatureLanguage detectedLanguage,
        OcrStatus ocrStatus,
        Double ocrConfidence
) {
}

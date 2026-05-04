package com.example.springboot.dto;

import com.example.springboot.domain.OcrStatus;
import com.example.springboot.domain.SignatureLanguage;

public record SignatureResponse(
        Long signatureId,
        String recognizedText,
        String koreanText,
        String koreanMeaningText,
        SignatureLanguage detectedLanguage,
        OcrStatus ocrStatus,
        Double ocrConfidence
) {
}

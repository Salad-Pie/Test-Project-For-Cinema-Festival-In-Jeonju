package com.example.springboot.dto;

public record SignatureRenderRequest(
        String text,
        String fontFamily,
        Integer fontSize,
        Integer width,
        Integer height
) {
}

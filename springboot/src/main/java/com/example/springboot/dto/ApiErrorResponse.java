package com.example.springboot.dto;

public record ApiErrorResponse(
        String code,
        String message,
        String field
) {
}

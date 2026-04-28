package com.example.springboot.dto;

public record VerifyResponse(
        Long userId,
        String verifiedToken,
        boolean verified
) {
}

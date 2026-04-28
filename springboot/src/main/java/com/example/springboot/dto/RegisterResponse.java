package com.example.springboot.dto;

public record RegisterResponse(
        Long userId,
        String token,
        String qrUrl,
        String sentTo
) {
}

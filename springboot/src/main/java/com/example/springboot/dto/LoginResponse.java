package com.example.springboot.dto;

public record LoginResponse(
        Long userId,
        String registerToken,
        String tabletUrl,
        String sentTo,
        boolean existingUser,
        boolean codeSent
) {
}

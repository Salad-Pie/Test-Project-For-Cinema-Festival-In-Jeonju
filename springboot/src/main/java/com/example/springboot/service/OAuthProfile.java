package com.example.springboot.service;

public record OAuthProfile(
        String providerUserId,
        String name,
        String email,
        String phoneNumber
) {
}

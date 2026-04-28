package com.example.springboot.dto;

public record OAuthAuthorizeResponse(
        String provider,
        String authorizationUrl
) {
}

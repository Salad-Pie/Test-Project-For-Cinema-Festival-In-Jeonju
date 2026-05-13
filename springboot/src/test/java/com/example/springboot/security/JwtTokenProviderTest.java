package com.example.springboot.security;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "testSecretKeyWithAtLeast32CharactersLongForHS256";
    private final long expirationMinutes = 30;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, expirationMinutes);
    }

    @Test
    @DisplayName("토큰 생성 및 필드 검증")
    void generateToken() {
        // given
        Long userId = 123L;
        TokenType tokenType = TokenType.VERIFIED;

        // when
        String token = jwtTokenProvider.generateToken(userId, tokenType);

        // then
        assertNotNull(token);
        assertEquals(userId, jwtTokenProvider.extractUserId(token));
        assertEquals(tokenType, jwtTokenProvider.extractTokenType(token));
    }

    @Test
    @DisplayName("OAuth State 토큰 생성 및 검증")
    void generateOAuthStateToken() {
        // given
        String provider = "google";
        String redirect = "/home";

        // when
        String token = jwtTokenProvider.generateOAuthStateToken(provider, redirect);

        // then
        assertNotNull(token);
        assertEquals(provider, jwtTokenProvider.extractSubject(token));
        assertEquals(TokenType.OAUTH_STATE, jwtTokenProvider.extractTokenType(token));
        
        Claims claims = jwtTokenProvider.parse(token);
        assertEquals(redirect, claims.get("redirect", String.class));
    }

    @Test
    @DisplayName("유효하지 않은 토큰 파싱 시 예외 발생")
    void parse_InvalidToken() {
        // given
        String invalidToken = "invalid.token.string";

        // when & then
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.extractUserId(invalidToken));
    }
}

package com.example.springboot.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMinutes;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(Long userId, TokenType tokenType) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", tokenType.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();
    }

    public String generateOAuthStateToken(String provider) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(provider)
                .claim("type", TokenType.OAUTH_STATE.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(10, ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();
    }

    public String generateOAuthStateToken(String provider, String redirectPath) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(provider)
                .claim("type", TokenType.OAUTH_STATE.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(10, ChronoUnit.MINUTES)));

        if (redirectPath != null && !redirectPath.isBlank() && redirectPath.startsWith("/")) {
            builder.claim("redirect", redirectPath);
        }

        return builder.signWith(secretKey).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    public Long extractUserId(String token) {
        try {
            Claims claims = parse(token);
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid token.");
        }
    }

    public TokenType extractTokenType(String token) {
        try {
            Claims claims = parse(token);
            return TokenType.valueOf(claims.get("type", String.class));
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid token type.");
        }
    }

    public String extractSubject(String token) {
        try {
            Claims claims = parse(token);
            return claims.getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid token subject.");
        }
    }
}

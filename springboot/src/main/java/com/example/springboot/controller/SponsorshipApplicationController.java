package com.example.springboot.controller;

import com.example.springboot.dto.SponsorshipApplicationRequest;
import com.example.springboot.dto.SponsorshipApplicationResponse;
import com.example.springboot.service.SponsorshipApplicationService;
import com.example.springboot.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sponsorship-applications")
public class SponsorshipApplicationController {

    private final SponsorshipApplicationService service;
    private final JwtTokenProvider jwtTokenProvider;

    public SponsorshipApplicationController(SponsorshipApplicationService service, JwtTokenProvider jwtTokenProvider) {
        this.service = service;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public ResponseEntity<SponsorshipApplicationResponse> create(
            @Valid @RequestBody SponsorshipApplicationRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                userId = jwtTokenProvider.extractUserId(token);
            } catch (Exception e) {
                // Ignore invalid tokens for optional auth
            }
        }
        return ResponseEntity.ok(service.create(request, userId));
    }
}

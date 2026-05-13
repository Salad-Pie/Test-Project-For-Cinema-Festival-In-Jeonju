package com.example.springboot.controller;

import com.example.springboot.dto.ExperienceZoneSurveyRequest;
import com.example.springboot.dto.SimpleCreatedResponse;
import com.example.springboot.service.ExperienceZoneSurveyService;
import com.example.springboot.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/experience-zone-surveys")
public class ExperienceZoneSurveyController {

    private final ExperienceZoneSurveyService service;
    private final JwtTokenProvider jwtTokenProvider;

    public ExperienceZoneSurveyController(ExperienceZoneSurveyService service, JwtTokenProvider jwtTokenProvider) {
        this.service = service;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public ResponseEntity<SimpleCreatedResponse> create(
            @Valid @RequestBody ExperienceZoneSurveyRequest request,
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


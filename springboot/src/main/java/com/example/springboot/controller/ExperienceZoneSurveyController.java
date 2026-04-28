package com.example.springboot.controller;

import com.example.springboot.dto.ExperienceZoneSurveyRequest;
import com.example.springboot.dto.SimpleCreatedResponse;
import com.example.springboot.service.ExperienceZoneSurveyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/experience-zone-surveys")
public class ExperienceZoneSurveyController {

    private final ExperienceZoneSurveyService service;

    public ExperienceZoneSurveyController(ExperienceZoneSurveyService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SimpleCreatedResponse> create(@Valid @RequestBody ExperienceZoneSurveyRequest request) {
        return ResponseEntity.ok(service.create(request));
    }
}


package com.example.springboot.controller;

import com.example.springboot.dto.ExhibitionSurveyRequest;
import com.example.springboot.dto.SimpleCreatedResponse;
import com.example.springboot.service.ExhibitionSurveyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exhibition-surveys")
public class ExhibitionSurveyController {

    private final ExhibitionSurveyService service;

    public ExhibitionSurveyController(ExhibitionSurveyService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SimpleCreatedResponse> create(@Valid @RequestBody ExhibitionSurveyRequest request) {
        return ResponseEntity.ok(service.create(request));
    }
}


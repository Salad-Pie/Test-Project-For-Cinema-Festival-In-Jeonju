package com.example.springboot.controller;

import com.example.springboot.dto.SponsorshipApplicationRequest;
import com.example.springboot.dto.SponsorshipApplicationResponse;
import com.example.springboot.service.SponsorshipApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sponsorship-applications")
public class SponsorshipApplicationController {

    private final SponsorshipApplicationService service;

    public SponsorshipApplicationController(SponsorshipApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SponsorshipApplicationResponse> create(@Valid @RequestBody SponsorshipApplicationRequest request) {
        return ResponseEntity.ok(service.create(request));
    }
}

package com.example.springboot.controller;

import com.example.springboot.dto.ProjectRecruitmentReservationRequest;
import com.example.springboot.dto.ProjectRecruitmentReservationResponse;
import com.example.springboot.service.ProjectRecruitmentReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/project-recruitments")
public class ProjectRecruitmentReservationController {

    private final ProjectRecruitmentReservationService service;

    public ProjectRecruitmentReservationController(ProjectRecruitmentReservationService service) {
        this.service = service;
    }

    @PostMapping("/{projectKey}")
    public ResponseEntity<ProjectRecruitmentReservationResponse> create(
            @PathVariable String projectKey,
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ProjectRecruitmentReservationRequest request
    ) {
        return ResponseEntity.ok(service.create(projectKey, authorization, request));
    }
}


package com.example.springboot.controller;

import com.example.springboot.dto.StreetCollaborationReservationRequest;
import com.example.springboot.dto.StreetCollaborationReservationResponse;
import com.example.springboot.dto.StreetCollaborationAvailabilityResponse;
import com.example.springboot.service.StreetCollaborationReservationService;
import com.example.springboot.security.JwtTokenProvider;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/street-collaboration-reservations")
public class StreetCollaborationReservationController {

    private final StreetCollaborationReservationService service;
    private final JwtTokenProvider jwtTokenProvider;

    public StreetCollaborationReservationController(StreetCollaborationReservationService service, JwtTokenProvider jwtTokenProvider) {
        this.service = service;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public ResponseEntity<StreetCollaborationReservationResponse> create(
            @Valid @RequestBody StreetCollaborationReservationRequest request,
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

    @GetMapping("/availability")
    public ResponseEntity<StreetCollaborationAvailabilityResponse> availability(
            @RequestParam("reservationAt") LocalDateTime reservationAt
    ) {
        return ResponseEntity.ok(service.getAvailability(reservationAt));
    }
}

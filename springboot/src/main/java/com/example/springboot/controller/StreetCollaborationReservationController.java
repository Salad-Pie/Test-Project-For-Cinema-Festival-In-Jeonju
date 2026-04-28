package com.example.springboot.controller;

import com.example.springboot.dto.StreetCollaborationReservationRequest;
import com.example.springboot.dto.StreetCollaborationReservationResponse;
import com.example.springboot.dto.StreetCollaborationAvailabilityResponse;
import com.example.springboot.service.StreetCollaborationReservationService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/street-collaboration-reservations")
public class StreetCollaborationReservationController {

    private final StreetCollaborationReservationService service;

    public StreetCollaborationReservationController(StreetCollaborationReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<StreetCollaborationReservationResponse> create(
            @Valid @RequestBody StreetCollaborationReservationRequest request
    ) {
        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping("/availability")
    public ResponseEntity<StreetCollaborationAvailabilityResponse> availability(
            @RequestParam("reservationAt") LocalDateTime reservationAt
    ) {
        return ResponseEntity.ok(service.getAvailability(reservationAt));
    }
}

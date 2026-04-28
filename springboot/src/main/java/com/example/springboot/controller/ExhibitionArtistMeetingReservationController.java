package com.example.springboot.controller;

import com.example.springboot.dto.ExhibitionArtistMeetingReservationRequest;
import com.example.springboot.dto.ExhibitionArtistMeetingReservationResponse;
import com.example.springboot.service.ExhibitionArtistMeetingReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exhibition-artist-meeting-reservations")
public class ExhibitionArtistMeetingReservationController {

    private final ExhibitionArtistMeetingReservationService service;

    public ExhibitionArtistMeetingReservationController(ExhibitionArtistMeetingReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ExhibitionArtistMeetingReservationResponse> create(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ExhibitionArtistMeetingReservationRequest request
    ) {
        return ResponseEntity.ok(service.create(authorization, request));
    }
}

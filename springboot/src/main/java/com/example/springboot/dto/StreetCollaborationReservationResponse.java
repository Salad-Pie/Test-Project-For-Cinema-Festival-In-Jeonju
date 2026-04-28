package com.example.springboot.dto;

import java.time.LocalDateTime;

public record StreetCollaborationReservationResponse(
        Long id,
        String name,
        String phoneNumber,
        LocalDateTime reservationAt,
        LocalDateTime createdAt
) {
}

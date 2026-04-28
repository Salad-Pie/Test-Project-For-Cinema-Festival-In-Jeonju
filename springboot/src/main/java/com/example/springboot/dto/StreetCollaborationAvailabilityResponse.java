package com.example.springboot.dto;

import java.time.LocalDateTime;

public record StreetCollaborationAvailabilityResponse(
        LocalDateTime reservationAt,
        int currentCount,
        int capacity,
        int remaining,
        boolean available
) {
}

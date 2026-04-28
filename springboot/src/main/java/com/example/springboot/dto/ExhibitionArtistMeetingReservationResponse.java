package com.example.springboot.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ExhibitionArtistMeetingReservationResponse(
        Long id,
        Long userId,
        LocalDate date,
        LocalTime time,
        LocalDateTime createdAt
) {
}

package com.example.springboot.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record ExhibitionArtistMeetingReservationRequest(
        @NotNull(message = "date is required.")
        LocalDate date,
        @NotNull(message = "time is required.")
        LocalTime time
) {
}

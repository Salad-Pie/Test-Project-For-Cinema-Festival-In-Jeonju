package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

public record StreetCollaborationReservationRequest(
        @NotBlank(message = "name is required.")
        String name,
        @NotBlank(message = "phoneNumber is required.")
        @Pattern(regexp = "^[0-9\\-+() ]{8,20}$", message = "phoneNumber format is invalid.")
        String phoneNumber,
        @NotNull(message = "reservationAt is required.")
        LocalDateTime reservationAt
) {
}

package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;

public record ProjectRecruitmentReservationRequest(
        @NotBlank(message = "phoneNumber is required.")
        @Pattern(regexp = "^[0-9\\-+() ]{8,20}$", message = "phoneNumber format is invalid.")
        String phoneNumber,
        @NotNull(message = "date is required.")
        LocalDate date,
        @NotNull(message = "time is required.")
        LocalTime time
) {}


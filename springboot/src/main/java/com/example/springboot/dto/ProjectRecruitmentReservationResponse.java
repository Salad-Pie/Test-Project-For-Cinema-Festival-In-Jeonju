package com.example.springboot.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ProjectRecruitmentReservationResponse(
        Long id,
        Long userId,
        String projectKey,
        String phoneNumber,
        LocalDate date,
        LocalTime time,
        LocalDateTime createdAt
) {}


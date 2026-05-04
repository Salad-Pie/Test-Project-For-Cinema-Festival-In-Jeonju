package com.example.springboot.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminReservationResponse(
        String type,
        Long id,
        String name,
        String phoneNumber,
        String userEmail,
        String projectKey,
        LocalDate date,
        LocalTime time,
        Long amount,
        String paymentMethodType,
        String paymentProviderName,
        String bankAccountMasked,
        LocalDateTime createdAt
) {
}

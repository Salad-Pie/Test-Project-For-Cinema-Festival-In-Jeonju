package com.example.springboot.dto;

public record AdminReservationTypeResponse(
        String type,
        String label,
        long count
) {
}

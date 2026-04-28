package com.example.springboot.dto;

public record SponsorshipApplicationResponse(
        Long id,
        String name,
        String phoneNumber,
        String bankAccount,
        Long amount,
        String address
) {
}

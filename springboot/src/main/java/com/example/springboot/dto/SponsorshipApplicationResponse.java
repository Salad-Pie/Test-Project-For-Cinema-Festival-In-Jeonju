package com.example.springboot.dto;

public record SponsorshipApplicationResponse(
        Long id,
        String name,
        String phoneNumber,
        String bankAccount,
        String paymentMethodType,
        String paymentProviderName,
        Long amount,
        String address
) {
}

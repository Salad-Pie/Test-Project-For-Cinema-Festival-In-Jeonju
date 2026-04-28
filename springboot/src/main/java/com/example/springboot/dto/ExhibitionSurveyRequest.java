package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ExhibitionSurveyRequest(
        @NotBlank(message = "name is required.")
        String name,
        @NotBlank(message = "phoneNumber is required.")
        @Pattern(regexp = "^[0-9\\-+() ]{8,20}$", message = "phoneNumber format is invalid.")
        String phoneNumber,
        @NotBlank(message = "address is required.")
        String address,
        @Pattern(regexp = "^$|^\\d{6}$", message = "identifierCode must be 6 digits when provided.")
        String identifierCode,
        @NotBlank(message = "impressivePoint is required.")
        String impressivePoint,
        @NotBlank(message = "improvementNeeded is required.")
        String improvementNeeded,
        @NotBlank(message = "desiredGenre is required.")
        String desiredGenre,
        @NotBlank(message = "invitedArtist is required.")
        String invitedArtist,
        @NotBlank(message = "feedback is required.")
        String feedback
) {}

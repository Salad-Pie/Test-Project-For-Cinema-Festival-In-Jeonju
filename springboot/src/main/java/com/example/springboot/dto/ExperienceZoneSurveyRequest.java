package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ExperienceZoneSurveyRequest(
        @NotBlank(message = "name is required.")
        String name,
        @NotBlank(message = "phoneNumber is required.")
        @Pattern(regexp = "^[0-9\\-+() ]{8,20}$", message = "phoneNumber format is invalid.")
        String phoneNumber,
        @NotBlank(message = "address is required.")
        String address,
        @NotBlank(message = "impressiveSpace is required.")
        String impressiveSpace,
        @NotBlank(message = "improvementIdeaSpace is required.")
        String improvementIdeaSpace,
        @NotBlank(message = "streamingParticipation is required.")
        String streamingParticipation,
        @NotBlank(message = "desiredGoods is required.")
        String desiredGoods,
        @NotBlank(message = "feedback is required.")
        String feedback
) {}


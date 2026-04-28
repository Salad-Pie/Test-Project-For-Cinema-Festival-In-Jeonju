package com.example.springboot.dto;

import com.example.springboot.domain.SignupProvider;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        SignupProvider provider,
        String providerUserId,
        String email,
        @Pattern(regexp = "^[0-9+\\-]{8,20}$", message = "전화번호 형식이 올바르지 않습니다.")
        String phoneNumber
) {
}

package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyCodeRequest(
        @NotBlank(message = "JWT 토큰은 필수입니다.")
        String token,
        @NotBlank(message = "식별자는 필수입니다.")
        @Pattern(regexp = "^[0-9]{6}$", message = "식별자는 6자리 숫자입니다.")
        String code
) {
}

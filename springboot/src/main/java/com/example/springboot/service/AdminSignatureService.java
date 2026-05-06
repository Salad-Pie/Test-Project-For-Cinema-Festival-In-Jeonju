package com.example.springboot.service;

import com.example.springboot.domain.Signature;
import com.example.springboot.domain.User;
import com.example.springboot.dto.AdminSignatureResponse;
import com.example.springboot.repository.SignatureRepository;
import com.example.springboot.repository.UserRepository;
import com.example.springboot.security.JwtTokenProvider;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminSignatureService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final SignatureRepository signatureRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AdminSignatureService(
            SignatureRepository signatureRepository,
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.signatureRepository = signatureRepository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public List<AdminSignatureResponse> signatures(String authorization) {
        requireAdmin(authorization);
        return signatureRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt")).stream()
                .map(this::toResponse)
                .toList();
    }

    private void requireAdmin(String authorization) {
        String token = extractBearerToken(authorization);
        Long userId = jwtTokenProvider.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found."));
        if (!ROLE_ADMIN.equals(user.getRole())) {
            throw new SecurityException("admin role is required.");
        }
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("authorization bearer token is required.");
        }
        return authorization.substring("Bearer ".length()).trim();
    }

    private AdminSignatureResponse toResponse(Signature item) {
        return new AdminSignatureResponse(
                item.getId(),
                item.getUser().getId(),
                item.getOriginalName(),
                item.getRecognizedText(),
                item.getEnglishName(),
                item.getKoreanName(),
                item.getKoreanMeaningText(),
                item.getNameLanguage() == null ? null : item.getNameLanguage().name(),
                item.getDetectedLanguage() == null ? null : item.getDetectedLanguage().name(),
                item.getNameConversionSource() == null ? null : item.getNameConversionSource().name(),
                item.getOcrStatus() == null ? null : item.getOcrStatus().name(),
                item.getOcrConfidence(),
                item.getOcrProcessedAt(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}

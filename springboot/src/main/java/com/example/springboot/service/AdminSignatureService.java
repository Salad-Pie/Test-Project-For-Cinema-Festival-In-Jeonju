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
    private final S3UploadService s3UploadService;

    public AdminSignatureService(
            SignatureRepository signatureRepository,
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider,
            S3UploadService s3UploadService
    ) {
        this.signatureRepository = signatureRepository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.s3UploadService = s3UploadService;
    }

    public List<AdminSignatureResponse> signatures(String authorization) {
        requireAdmin(authorization);
        return signatureRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt")).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AdminSignatureResponse> lowConfidenceSignatures(String authorization, Double threshold) {
        requireAdmin(authorization);
        return signatureRepository.findByOcrConfidenceLessThan(threshold).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdminSignatureResponse updateSignature(String authorization, Long id, String correctedText, String status) {
        requireAdmin(authorization);
        Signature signature = signatureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Signature not found."));
        
        if (correctedText != null) {
            signature.setKoreanName(correctedText); 
            signature.setNameConversionSource(com.example.springboot.domain.NameConversionSource.MANUAL);
        }
        
        if (status != null) {
            signature.setOcrStatus(com.example.springboot.domain.OcrStatus.valueOf(status.toUpperCase()));
        }
        
        return toResponse(signature);
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
        String imageUrl = null;
        if (item.getOriginalS3Key() != null) {
            try {
                imageUrl = s3UploadService.createPresignedDownloadUrl(item.getOriginalS3Key());
            } catch (Exception e) {
                // Log error or ignore
            }
        }

        return new AdminSignatureResponse(
                item.getId(),
                item.getUser().getId(),
                imageUrl,
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

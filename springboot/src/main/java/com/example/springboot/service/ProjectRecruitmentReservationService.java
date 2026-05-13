package com.example.springboot.service;

import com.example.springboot.domain.ProjectRecruitmentReservation;
import com.example.springboot.domain.User;
import com.example.springboot.dto.ProjectRecruitmentReservationRequest;
import com.example.springboot.dto.ProjectRecruitmentReservationResponse;
import com.example.springboot.exception.BusinessException;
import com.example.springboot.exception.ErrorCode;
import com.example.springboot.repository.ProjectRecruitmentReservationRepository;
import com.example.springboot.repository.UserRepository;
import com.example.springboot.security.JwtTokenProvider;
import com.example.springboot.security.TokenType;
import com.example.springboot.util.PhoneUtils;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectRecruitmentReservationService {

    private final ProjectRecruitmentReservationRepository repository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PointRewardService pointRewardService;
    private final PhoneUtils phoneUtils;

    private static final String AX_SHOP_SHOP_PROJECT_KEY = "ax-shop-shop";
    private static final String PHONE_NUMBER_PATTERN = "^[0-9\\-+() ]{8,20}$";

    private static final Map<String, Slot> ALLOWED_SLOT_BY_PROJECT = Map.of(
            "ax-space", new Slot(LocalDate.of(2026, 5, 8), LocalTime.of(20, 0)),
            "k-art-ax", new Slot(LocalDate.of(2026, 5, 9), LocalTime.of(16, 0)),
            AX_SHOP_SHOP_PROJECT_KEY, new Slot(LocalDate.of(2026, 5, 9), LocalTime.of(14, 0)),
            "pd-writer-edit-sound", new Slot(LocalDate.of(2026, 5, 9), LocalTime.of(19, 0))
    );

    public ProjectRecruitmentReservationResponse create(
            String projectKey,
            String authorization,
            ProjectRecruitmentReservationRequest request
    ) {
        Slot allowedSlot = ALLOWED_SLOT_BY_PROJECT.get(projectKey);
        if (allowedSlot == null) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_SLOT);
        }
        if (!allowedSlot.date().equals(request.date()) || !allowedSlot.time().equals(request.time())) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_SLOT);
        }

        String token = extractBearerToken(authorization);
        TokenType tokenType = jwtTokenProvider.extractTokenType(token);
        if (tokenType != TokenType.REGISTER && tokenType != TokenType.VERIFIED) {
            throw new IllegalArgumentException("token type is not valid for project recruitment.");
        }

        Long userId = jwtTokenProvider.extractUserId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("user not found."));

        boolean exists = repository.existsByProjectKeyAndUserIdAndDateAndTime(projectKey, userId, request.date(), request.time());
        if (exists) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }

        validatePhoneNumber(projectKey, request.phoneNumber());

        ProjectRecruitmentReservation entity = new ProjectRecruitmentReservation();
        entity.setProjectKey(projectKey);
        entity.setUser(user);
        entity.setPhoneNumber(phoneUtils.normalize(request.phoneNumber()));
        entity.setDate(request.date());
        entity.setTime(request.time());
        ProjectRecruitmentReservation saved = repository.save(entity);
        pointRewardService.earnActivityPoints(userId, "프로젝트 참여 신청 완료 (" + projectKey + ")");

        return new ProjectRecruitmentReservationResponse(
                saved.getId(), saved.getUser().getId(), saved.getProjectKey(),
                saved.getPhoneNumber(), saved.getDate(), saved.getTime(), saved.getCreatedAt()
        );
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("authorization bearer token is required.");
        }
        return authorization.substring("Bearer ".length()).trim();
    }

    private void validatePhoneNumber(String projectKey, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            if (AX_SHOP_SHOP_PROJECT_KEY.equals(projectKey)) {
                return;
            }
            throw new IllegalArgumentException("phoneNumber is required.");
        }
        if (!phoneNumber.matches(PHONE_NUMBER_PATTERN)) {
            throw new IllegalArgumentException("phoneNumber format is invalid.");
        }
    }

    private record Slot(LocalDate date, LocalTime time) {}
}

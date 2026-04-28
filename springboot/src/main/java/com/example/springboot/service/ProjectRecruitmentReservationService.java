package com.example.springboot.service;

import com.example.springboot.domain.ProjectRecruitmentReservation;
import com.example.springboot.domain.User;
import com.example.springboot.dto.ProjectRecruitmentReservationRequest;
import com.example.springboot.dto.ProjectRecruitmentReservationResponse;
import com.example.springboot.repository.ProjectRecruitmentReservationRepository;
import com.example.springboot.repository.UserRepository;
import com.example.springboot.security.JwtTokenProvider;
import com.example.springboot.security.TokenType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectRecruitmentReservationService {

    private static final Map<String, Slot> ALLOWED_SLOT_BY_PROJECT = Map.of(
            "ax-space", new Slot(LocalDate.of(2026, 5, 8), LocalTime.of(20, 0)),
            "k-art-ax", new Slot(LocalDate.of(2026, 5, 9), LocalTime.of(16, 0)),
            "ax-shop-shop", new Slot(LocalDate.of(2026, 5, 9), LocalTime.of(14, 0)),
            "pd-writer-edit-sound", new Slot(LocalDate.of(2026, 5, 9), LocalTime.of(19, 0))
    );

    private final ProjectRecruitmentReservationRepository repository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public ProjectRecruitmentReservationService(
            ProjectRecruitmentReservationRepository repository,
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public ProjectRecruitmentReservationResponse create(
            String projectKey,
            String authorization,
            ProjectRecruitmentReservationRequest request
    ) {
        Slot allowedSlot = ALLOWED_SLOT_BY_PROJECT.get(projectKey);
        if (allowedSlot == null) {
            throw new IllegalArgumentException("unsupported projectKey: " + projectKey);
        }
        if (!allowedSlot.date().equals(request.date()) || !allowedSlot.time().equals(request.time())) {
            throw new IllegalArgumentException("reservation is available only for predefined slot.");
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
            throw new IllegalArgumentException("duplicate reservation: same user already reserved this project slot.");
        }

        ProjectRecruitmentReservation entity = new ProjectRecruitmentReservation();
        entity.setProjectKey(projectKey);
        entity.setUser(user);
        entity.setPhoneNumber(normalizePhone(request.phoneNumber()));
        entity.setDate(request.date());
        entity.setTime(request.time());
        ProjectRecruitmentReservation saved = repository.save(entity);

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

    private String normalizePhone(String phoneNumber) {
        String digits = phoneNumber.replaceAll("[^0-9]", "");
        if (digits.length() == 11) return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        if (digits.length() == 10) return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        return phoneNumber.trim();
    }

    private record Slot(LocalDate date, LocalTime time) {}
}


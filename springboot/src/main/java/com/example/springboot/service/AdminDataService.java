package com.example.springboot.service;

import com.example.springboot.domain.User;
import com.example.springboot.repository.*;
import com.example.springboot.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDataService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SignatureRepository signatureRepository;
    private final ExhibitionSurveyRepository exhibitionSurveyRepository;
    private final ExperienceZoneSurveyRepository experienceZoneSurveyRepository;
    private final SponsorshipApplicationRepository sponsorshipApplicationRepository;
    private final StreetCollaborationReservationRepository streetCollaborationReservationRepository;
    private final ProjectRecruitmentReservationRepository projectRecruitmentReservationRepository;
    private final IdeaContestRepository ideaContestRepository;

    public List<?> getData(String authorization, String entityType) {
        requireAdmin(authorization);
        return switch (entityType) {
            case "users" -> userRepository.findAll();
            case "signatures" -> signatureRepository.findAll();
            case "exhibition-surveys" -> exhibitionSurveyRepository.findAll();
            case "experience-zone-surveys" -> experienceZoneSurveyRepository.findAll();
            case "sponsorship-applications" -> sponsorshipApplicationRepository.findAll();
            case "street-collaboration-reservations" -> streetCollaborationReservationRepository.findAll();
            case "project-recruitments" -> projectRecruitmentReservationRepository.findAll();
            case "idea-contests" -> ideaContestRepository.findAll();
            default -> throw new IllegalArgumentException("Unknown entity type: " + entityType);
        };
    }

    private void requireAdmin(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("authorization bearer token is required.");
        }
        String token = authorization.substring(7);
        Long userId = jwtTokenProvider.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found."));
        if (!"ADMIN".equals(user.getRole())) {
            throw new SecurityException("admin role is required.");
        }
    }
}

package com.example.springboot.service;

import com.example.springboot.domain.ExhibitionArtistMeetingReservation;
import com.example.springboot.domain.ExhibitionSurvey;
import com.example.springboot.domain.ExperienceZoneSurvey;
import com.example.springboot.domain.ProjectRecruitmentReservation;
import com.example.springboot.domain.SponsorshipApplication;
import com.example.springboot.domain.StreetCollaborationReservation;
import com.example.springboot.domain.User;
import com.example.springboot.dto.AdminReservationResponse;
import com.example.springboot.dto.AdminReservationTypeResponse;
import com.example.springboot.repository.ExhibitionArtistMeetingReservationRepository;
import com.example.springboot.repository.ExhibitionSurveyRepository;
import com.example.springboot.repository.ExperienceZoneSurveyRepository;
import com.example.springboot.repository.ProjectRecruitmentReservationRepository;
import com.example.springboot.repository.SponsorshipApplicationRepository;
import com.example.springboot.repository.StreetCollaborationReservationRepository;
import com.example.springboot.repository.UserRepository;
import com.example.springboot.security.JwtTokenProvider;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminReservationService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String STREET_COLLABORATION = "STREET_COLLABORATION";
    private static final String ARTIST_MEETING = "ARTIST_MEETING";
    private static final String PROJECT_RECRUITMENT = "PROJECT_RECRUITMENT";
    private static final String SPONSORSHIP = "SPONSORSHIP";
    private static final String EXHIBITION_SURVEY = "EXHIBITION_SURVEY";
    private static final String EXPERIENCE_ZONE_SURVEY = "EXPERIENCE_ZONE_SURVEY";

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final StreetCollaborationReservationRepository streetRepository;
    private final ExhibitionArtistMeetingReservationRepository artistMeetingRepository;
    private final ProjectRecruitmentReservationRepository projectRecruitmentRepository;
    private final SponsorshipApplicationRepository sponsorshipRepository;
    private final ExhibitionSurveyRepository exhibitionSurveyRepository;
    private final ExperienceZoneSurveyRepository experienceZoneSurveyRepository;

    public AdminReservationService(
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider,
            StreetCollaborationReservationRepository streetRepository,
            ExhibitionArtistMeetingReservationRepository artistMeetingRepository,
            ProjectRecruitmentReservationRepository projectRecruitmentRepository,
            SponsorshipApplicationRepository sponsorshipRepository,
            ExhibitionSurveyRepository exhibitionSurveyRepository,
            ExperienceZoneSurveyRepository experienceZoneSurveyRepository
    ) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.streetRepository = streetRepository;
        this.artistMeetingRepository = artistMeetingRepository;
        this.projectRecruitmentRepository = projectRecruitmentRepository;
        this.sponsorshipRepository = sponsorshipRepository;
        this.exhibitionSurveyRepository = exhibitionSurveyRepository;
        this.experienceZoneSurveyRepository = experienceZoneSurveyRepository;
    }

    public List<AdminReservationTypeResponse> types(String authorization) {
        requireAdmin(authorization);
        return List.of(
                new AdminReservationTypeResponse(STREET_COLLABORATION, "영화의 거리 소상공인 협력", streetRepository.count()),
                new AdminReservationTypeResponse(ARTIST_MEETING, "전시 작가와의 만남", artistMeetingRepository.count()),
                new AdminReservationTypeResponse(PROJECT_RECRUITMENT, "프로젝트 모집", projectRecruitmentRepository.count()),
                new AdminReservationTypeResponse(SPONSORSHIP, "후원 상품 신청", sponsorshipRepository.count()),
                new AdminReservationTypeResponse(EXHIBITION_SURVEY, "전시 설문", exhibitionSurveyRepository.count()),
                new AdminReservationTypeResponse(EXPERIENCE_ZONE_SURVEY, "체험존 설문", experienceZoneSurveyRepository.count())
        );
    }

    public List<AdminReservationResponse> reservations(
            String authorization,
            String type,
            LocalDate date,
            LocalTime time,
            String projectKey
    ) {
        requireAdmin(authorization);
        String normalizedType = type == null || type.isBlank() ? "" : type.trim().toUpperCase();
        List<AdminReservationResponse> responses = new ArrayList<>();

        if (normalizedType.isBlank() || STREET_COLLABORATION.equals(normalizedType)) {
            responses.addAll(streetRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                    .filter(item -> date == null || item.getReservationAt().toLocalDate().equals(date))
                    .filter(item -> time == null || item.getReservationAt().toLocalTime().equals(time))
                    .map(this::toStreetResponse)
                    .toList());
        }
        if (normalizedType.isBlank() || ARTIST_MEETING.equals(normalizedType)) {
            responses.addAll(artistMeetingRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                    .filter(item -> date == null || item.getDate().equals(date))
                    .filter(item -> time == null || item.getTime().equals(time))
                    .map(this::toArtistMeetingResponse)
                    .toList());
        }
        if (normalizedType.isBlank() || PROJECT_RECRUITMENT.equals(normalizedType)) {
            responses.addAll(projectRecruitmentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                    .filter(item -> date == null || item.getDate().equals(date))
                    .filter(item -> time == null || item.getTime().equals(time))
                    .filter(item -> projectKey == null || projectKey.isBlank() || item.getProjectKey().equals(projectKey.trim()))
                    .map(this::toProjectRecruitmentResponse)
                    .toList());
        }
        if (normalizedType.isBlank() || SPONSORSHIP.equals(normalizedType)) {
            responses.addAll(sponsorshipRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                    .filter(item -> date == null || item.getCreatedAt().toLocalDate().equals(date))
                    .map(this::toSponsorshipResponse)
                    .toList());
        }
        if (normalizedType.isBlank() || EXHIBITION_SURVEY.equals(normalizedType)) {
            responses.addAll(exhibitionSurveyRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                    .filter(item -> date == null || item.getCreatedAt().toLocalDate().equals(date))
                    .map(this::toExhibitionSurveyResponse)
                    .toList());
        }
        if (normalizedType.isBlank() || EXPERIENCE_ZONE_SURVEY.equals(normalizedType)) {
            responses.addAll(experienceZoneSurveyRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                    .filter(item -> date == null || item.getCreatedAt().toLocalDate().equals(date))
                    .map(this::toExperienceZoneSurveyResponse)
                    .toList());
        }

        return responses;
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

    private AdminReservationResponse toStreetResponse(StreetCollaborationReservation item) {
        return new AdminReservationResponse(
                STREET_COLLABORATION, item.getId(), item.getName(), item.getPhoneNumber(), null, null,
                item.getReservationAt().toLocalDate(), item.getReservationAt().toLocalTime(), null, null, null, null, item.getCreatedAt()
        );
    }

    private AdminReservationResponse toArtistMeetingResponse(ExhibitionArtistMeetingReservation item) {
        User user = item.getUser();
        return new AdminReservationResponse(
                ARTIST_MEETING, item.getId(), user.getName(), user.getPhoneNumber(), user.getEmail(), null,
                item.getDate(), item.getTime(), null, null, null, null, item.getCreatedAt()
        );
    }

    private AdminReservationResponse toProjectRecruitmentResponse(ProjectRecruitmentReservation item) {
        User user = item.getUser();
        return new AdminReservationResponse(
                PROJECT_RECRUITMENT, item.getId(), user.getName(), item.getPhoneNumber(), user.getEmail(), item.getProjectKey(),
                item.getDate(), item.getTime(), null, null, null, null, item.getCreatedAt()
        );
    }

    private AdminReservationResponse toSponsorshipResponse(SponsorshipApplication item) {
        return new AdminReservationResponse(
                SPONSORSHIP, item.getId(), item.getName(), item.getPhoneNumber(), null, null,
                item.getCreatedAt().toLocalDate(), null, item.getAmount(), item.getPaymentMethodType(),
                item.getPaymentProviderName(), item.getBankAccountMasked(), item.getCreatedAt()
        );
    }

    private AdminReservationResponse toExhibitionSurveyResponse(ExhibitionSurvey item) {
        return new AdminReservationResponse(
                EXHIBITION_SURVEY, item.getId(), item.getName(), item.getPhoneNumber(), null, null,
                item.getCreatedAt().toLocalDate(), null, null, null, null, null, item.getCreatedAt()
        );
    }

    private AdminReservationResponse toExperienceZoneSurveyResponse(ExperienceZoneSurvey item) {
        return new AdminReservationResponse(
                EXPERIENCE_ZONE_SURVEY, item.getId(), item.getName(), item.getPhoneNumber(), null, null,
                item.getCreatedAt().toLocalDate(), null, null, null, null, null, item.getCreatedAt()
        );
    }
}

package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.springboot.dto.admin.AdminStatisticsResponse;
import com.example.springboot.repository.*;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminStatisticsServiceTest {

    private AdminStatisticsService adminStatisticsService;

    @Mock private UserRepository userRepository;
    @Mock private SignatureRepository signatureRepository;
    @Mock private StreetCollaborationReservationRepository streetRepo;
    @Mock private ExhibitionArtistMeetingReservationRepository artistRepo;
    @Mock private ProjectRecruitmentReservationRepository projectRepo;
    @Mock private SponsorshipApplicationRepository sponsorshipRepo;
    @Mock private ExhibitionSurveyRepository exhibitionSurveyRepo;
    @Mock private ExperienceZoneSurveyRepository experienceSurveyRepo;

    @BeforeEach
    void setUp() {
        adminStatisticsService = new AdminStatisticsService(
                userRepository,
                signatureRepository,
                streetRepo,
                artistRepo,
                projectRepo,
                sponsorshipRepo,
                exhibitionSurveyRepo,
                experienceSurveyRepo
        );
    }

    @Test
    @DisplayName("통계 정보 요약 조회 성공")
    void getStatistics() {
        // given
        when(userRepository.count()).thenReturn(100L);
        when(signatureRepository.count()).thenReturn(50L);
        when(streetRepo.count()).thenReturn(10L);
        when(artistRepo.count()).thenReturn(5L);
        when(projectRepo.count()).thenReturn(3L);
        when(sponsorshipRepo.count()).thenReturn(2L);
        when(exhibitionSurveyRepo.count()).thenReturn(20L);
        when(experienceSurveyRepo.count()).thenReturn(15L);
        
        when(signatureRepository.findAll()).thenReturn(Collections.emptyList());
        when(streetRepo.findAll()).thenReturn(Collections.emptyList());
        when(artistRepo.findAll()).thenReturn(Collections.emptyList());
        when(projectRepo.findAll()).thenReturn(Collections.emptyList());
        when(sponsorshipRepo.findAll()).thenReturn(Collections.emptyList());

        // when
        AdminStatisticsResponse result = adminStatisticsService.getStatistics();

        // then
        assertEquals(100L, result.totalUsers());
        assertEquals(50L, result.totalSignatures());
        assertEquals(20L, result.totalReservations()); // 10+5+3+2
        assertEquals(10L, result.reservationsByType().get("Street Collaboration"));
        assertEquals(20L, result.reservationsByType().get("Exhibition Survey"));
    }
}

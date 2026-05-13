package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.springboot.domain.StreetCollaborationReservation;
import com.example.springboot.domain.User;
import com.example.springboot.dto.AdminReservationResponse;
import com.example.springboot.repository.*;
import com.example.springboot.security.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class AdminReservationServiceTest {

    private AdminReservationService adminReservationService;

    @Mock private UserRepository userRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private StreetCollaborationReservationRepository streetRepository;
    @Mock private ExhibitionArtistMeetingReservationRepository artistMeetingRepository;
    @Mock private ProjectRecruitmentReservationRepository projectRecruitmentRepository;
    @Mock private SponsorshipApplicationRepository sponsorshipRepository;
    @Mock private ExhibitionSurveyRepository exhibitionSurveyRepository;
    @Mock private ExperienceZoneSurveyRepository experienceZoneSurveyRepository;

    @BeforeEach
    void setUp() {
        adminReservationService = new AdminReservationService(
                userRepository,
                jwtTokenProvider,
                streetRepository,
                artistMeetingRepository,
                projectRecruitmentRepository,
                sponsorshipRepository,
                exhibitionSurveyRepository,
                experienceZoneSurveyRepository
        );
    }

    @Test
    @DisplayName("전체 예약 목록 조회 - 관리자 권한 필수")
    void reservations_AdminRequired() {
        // given
        String token = "Bearer admin-token";
        User user = new User();
        user.setRole("USER"); // Not Admin
        
        when(jwtTokenProvider.extractUserId(anyString())).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        assertThrows(SecurityException.class, () -> 
                adminReservationService.reservations(token, null, null, null, null));
    }

    @Test
    @DisplayName("특정 타입(Street Collaboration) 예약 목록 조회 성공")
    void reservations_StreetCollaboration() {
        // given
        String token = "Bearer admin-token";
        User admin = new User();
        admin.setRole("ADMIN");
        
        StreetCollaborationReservation reservation = StreetCollaborationReservation.builder()
                .id(10L)
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .reservationAt(LocalDateTime.of(2026, 5, 13, 14, 0))
                .createdAt(LocalDateTime.now())
                .build();

        when(jwtTokenProvider.extractUserId(anyString())).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(streetRepository.findAll(any(Sort.class))).thenReturn(List.of(reservation));

        // when
        List<AdminReservationResponse> result = adminReservationService.reservations(token, "STREET_COLLABORATION", null, null, null);

        // then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("STREET_COLLABORATION", result.get(0).type());
        assertEquals("Test User", result.get(0).name());
    }
}

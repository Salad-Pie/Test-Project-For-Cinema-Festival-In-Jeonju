package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.springboot.domain.ProjectRecruitmentReservation;
import com.example.springboot.domain.User;
import com.example.springboot.dto.ProjectRecruitmentReservationRequest;
import com.example.springboot.dto.ProjectRecruitmentReservationResponse;
import com.example.springboot.exception.BusinessException;
import com.example.springboot.repository.ProjectRecruitmentReservationRepository;
import com.example.springboot.repository.UserRepository;
import com.example.springboot.security.JwtTokenProvider;
import com.example.springboot.security.TokenType;
import com.example.springboot.util.PhoneUtils;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectRecruitmentReservationServiceTest {

    private ProjectRecruitmentReservationService service;

    @Mock private ProjectRecruitmentReservationRepository repository;
    @Mock private UserRepository userRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private PointRewardService pointRewardService;
    private final PhoneUtils phoneUtils = new PhoneUtils();

    @BeforeEach
    void setUp() {
        service = new ProjectRecruitmentReservationService(
                repository, userRepository, jwtTokenProvider, pointRewardService, phoneUtils
        );
    }

    @Test
    @DisplayName("프로젝트 예약 성공")
    void create_Success() {
        // given
        String projectKey = "ax-space";
        String auth = "Bearer valid-token";
        ProjectRecruitmentReservationRequest request = new ProjectRecruitmentReservationRequest(
                "010-1234-5678", LocalDate.of(2026, 5, 8), LocalTime.of(20, 0)
        );

        User user = User.builder().id(1L).build();

        when(jwtTokenProvider.extractTokenType(anyString())).thenReturn(TokenType.VERIFIED);
        when(jwtTokenProvider.extractUserId(anyString())).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.existsByProjectKeyAndUserIdAndDateAndTime(anyString(), anyLong(), any(), any()))
                .thenReturn(false);

        ProjectRecruitmentReservation saved = ProjectRecruitmentReservation.builder()
                .id(10L)
                .user(user)
                .projectKey(projectKey)
                .build();
        
        when(repository.save(any(ProjectRecruitmentReservation.class))).thenReturn(saved);

        // when
        ProjectRecruitmentReservationResponse response = service.create(projectKey, auth, request);

        // then
        assertNotNull(response);
        assertEquals(10L, response.id());
        verify(pointRewardService).earnActivityPoints(eq(1L), anyString());
    }

    @Test
    @DisplayName("잘못된 예약 시간 선택 시 예외 발생")
    void create_InvalidSlot() {
        // given
        String projectKey = "ax-space";
        ProjectRecruitmentReservationRequest request = new ProjectRecruitmentReservationRequest(
                "010-1234-5678", LocalDate.of(2026, 5, 8), LocalTime.of(21, 0) // 21:00 is not allowed for ax-space
        );

        // when & then
        assertThrows(BusinessException.class, () -> service.create(projectKey, "Bearer token", request));
    }
}

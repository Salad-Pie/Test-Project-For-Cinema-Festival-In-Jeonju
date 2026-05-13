package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.springboot.domain.ExperienceZoneSurvey;
import com.example.springboot.dto.ExperienceZoneSurveyRequest;
import com.example.springboot.dto.SimpleCreatedResponse;
import com.example.springboot.exception.BusinessException;
import com.example.springboot.repository.ExperienceZoneSurveyRepository;
import com.example.springboot.util.PhoneUtils;
import java.time.Clock;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExperienceZoneSurveyServiceTest {

    private ExperienceZoneSurveyService experienceZoneSurveyService;

    @Mock private ExperienceZoneSurveyRepository repository;
    @Mock private PointRewardService pointRewardService;
    private final PhoneUtils phoneUtils = new PhoneUtils();
    private final Clock clock = Clock.systemDefaultZone();

    @BeforeEach
    void setUp() {
        experienceZoneSurveyService = new ExperienceZoneSurveyService(
                repository, pointRewardService, phoneUtils, clock
        );
    }

    @Test
    @DisplayName("체험존 설문 제출 성공")
    void create_Success() {
        // given
        ExperienceZoneSurveyRequest request = new ExperienceZoneSurveyRequest(
                "Test User", "01012345678", "Seoul", "Space", "Idea", "Yes", "Goods", "Feedback"
        );
        Long userId = 1L;
        
        when(repository.existsByNameAndPhoneNumberAndCreatedAtAfter(anyString(), anyString(), any(LocalDateTime.class)))
                .thenReturn(false);
        
        ExperienceZoneSurvey saved = ExperienceZoneSurvey.builder()
                .id(200L)
                .build();
        when(repository.save(any(ExperienceZoneSurvey.class))).thenReturn(saved);

        // when
        SimpleCreatedResponse response = experienceZoneSurveyService.create(request, userId);

        // then
        assertEquals(200L, response.id());
        verify(pointRewardService).earnActivityPoints(eq(userId), anyString());
    }

    @Test
    @DisplayName("중복 설문 제출 시 예외 발생")
    void create_Duplicate() {
        // given
        ExperienceZoneSurveyRequest request = new ExperienceZoneSurveyRequest(
                "Test User", "01012345678", "Seoul", "Space", "Idea", "Yes", "Goods", "Feedback"
        );
        
        when(repository.existsByNameAndPhoneNumberAndCreatedAtAfter(anyString(), anyString(), any(LocalDateTime.class)))
                .thenReturn(true);

        // when & then
        assertThrows(BusinessException.class, () -> experienceZoneSurveyService.create(request, 1L));
    }
}

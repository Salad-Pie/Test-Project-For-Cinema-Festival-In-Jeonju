package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.springboot.domain.ExhibitionSurvey;
import com.example.springboot.domain.IdentifierCode;
import com.example.springboot.domain.User;
import com.example.springboot.dto.ExhibitionSurveyRequest;
import com.example.springboot.dto.SimpleCreatedResponse;
import com.example.springboot.exception.BusinessException;
import com.example.springboot.repository.ExhibitionSurveyRepository;
import com.example.springboot.repository.IdentifierCodeRepository;
import com.example.springboot.util.PhoneUtils;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExhibitionSurveyServiceTest {

    private ExhibitionSurveyService exhibitionSurveyService;

    @Mock private ExhibitionSurveyRepository repository;
    @Mock private IdentifierCodeRepository identifierCodeRepository;
    @Mock private PointRewardService pointRewardService;
    private final PhoneUtils phoneUtils = new PhoneUtils();
    private final Clock clock = Clock.systemDefaultZone();

    @BeforeEach
    void setUp() {
        exhibitionSurveyService = new ExhibitionSurveyService(
                repository, identifierCodeRepository, pointRewardService, phoneUtils, clock
        );
    }

    @Test
    @DisplayName("전시 설문 제출 성공")
    void create_Success() {
        // given
        ExhibitionSurveyRequest request = new ExhibitionSurveyRequest(
                "Test User", "01012345678", "Seoul", "123456", 
                "Point", "Improvement", "Genre", "Artist", "Feedback"
        );
        
        when(repository.existsByNameAndPhoneNumberAndCreatedAtAfter(anyString(), anyString(), any(LocalDateTime.class)))
                .thenReturn(false);
        
        ExhibitionSurvey saved = ExhibitionSurvey.builder()
                .id(100L)
                .build();
        when(repository.save(any(ExhibitionSurvey.class))).thenReturn(saved);

        User user = User.builder().id(1L).build();
        IdentifierCode code = new IdentifierCode(); code.setUser(user);
        when(identifierCodeRepository.findTopByCodeOrderByIdDesc("123456")).thenReturn(Optional.of(code));

        // when
        SimpleCreatedResponse response = exhibitionSurveyService.create(request);

        // then
        assertEquals(100L, response.id());
        verify(pointRewardService).earnActivityPoints(eq(1L), anyString());
    }

    @Test
    @DisplayName("중복 설문 제출 시 예외 발생 (10분 이내)")
    void create_Duplicate() {
        // given
        ExhibitionSurveyRequest request = new ExhibitionSurveyRequest(
                "Test User", "01012345678", "Seoul", "123456", 
                "Point", "Improvement", "Genre", "Artist", "Feedback"
        );
        
        when(repository.existsByNameAndPhoneNumberAndCreatedAtAfter(anyString(), anyString(), any(LocalDateTime.class)))
                .thenReturn(true);

        // when & then
        assertThrows(BusinessException.class, () -> exhibitionSurveyService.create(request));
    }
}

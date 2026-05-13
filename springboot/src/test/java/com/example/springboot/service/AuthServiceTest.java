package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.springboot.domain.IdentifierCode;
import com.example.springboot.domain.SignupProvider;
import com.example.springboot.domain.User;
import com.example.springboot.dto.EmailLoginRequest;
import com.example.springboot.dto.LoginResponse;
import com.example.springboot.repository.IdentifierCodeRepository;
import com.example.springboot.repository.SignatureRepository;
import com.example.springboot.repository.UserRepository;
import com.example.springboot.security.JwtTokenProvider;
import com.example.springboot.security.TokenType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private IdentifierCodeRepository identifierCodeRepository;
    @Mock private SignatureRepository signatureRepository;
    @Mock private NotificationService notificationService;
    @Mock private GoogleVisionOcrService googleVisionOcrService;
    @Mock private S3UploadService s3UploadService;
    @Mock private EnglishKoreanNameService englishKoreanNameService;
    @Mock private GoogleTranslationService googleTranslationService;
    @Mock private SignatureImageService signatureImageService;
    @Mock private CertificatePdfService certificatePdfService;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private PointRewardService pointRewardService;

    private final String tabletBaseUrl = "http://tablet.test";

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                identifierCodeRepository,
                signatureRepository,
                notificationService,
                googleVisionOcrService,
                s3UploadService,
                englishKoreanNameService,
                googleTranslationService,
                signatureImageService,
                certificatePdfService,
                jwtTokenProvider,
                pointRewardService,
                tabletBaseUrl
        );
    }

    @Test
    @DisplayName("이메일 로그인 - 신규 유저 가입 및 토큰 발급")
    void loginEmail_NewUser() {
        // given
        EmailLoginRequest request = new EmailLoginRequest("Test User", "test@example.com", "010-1234-5678");
        when(userRepository.findFirstByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findFirstByPhoneNumber(anyString())).thenReturn(Optional.empty());
        
        User savedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        when(identifierCodeRepository.existsByCode(anyString())).thenReturn(false);
        when(jwtTokenProvider.generateToken(eq(1L), eq(TokenType.REGISTER))).thenReturn("test-token");

        // when
        LoginResponse response = authService.loginEmail(request);

        // then
        assertNotNull(response);
        assertEquals(1L, response.userId());
        assertEquals("test-token", response.registerToken());
        assertTrue(response.tabletUrl().contains("test-token"));
        verify(userRepository).save(any(User.class));
        verify(pointRewardService).earnActivityPoints(eq(1L), anyString());
    }

    @Test
    @DisplayName("이메일 로그인 - 기존 유저 정보 업데이트 및 토큰 발급")
    void loginEmail_ExistingUser() {
        // given
        EmailLoginRequest request = new EmailLoginRequest("Existing User", "existing@example.com", "010-9999-8888");
        User existingUser = User.builder()
                .id(2L)
                .email("existing@example.com")
                .isVerifiedIdentity(false)
                .build();
        
        when(userRepository.findFirstByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));
        when(jwtTokenProvider.generateToken(eq(2L), eq(TokenType.REGISTER))).thenReturn("existing-token");

        // when
        LoginResponse response = authService.loginEmail(request);

        // then
        assertEquals(2L, response.userId());
        assertEquals("existing-token", response.registerToken());
        assertTrue(response.existingUser());
        verify(userRepository, never()).save(any(User.class)); // mergeUser modifies in-place, JPA handles save if transactional (but here it's mock)
    }

    @Test
    @DisplayName("이메일 인증 코드 발송")
    void sendEmailLoginCode() {
        // given
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .build();
        when(userRepository.findFirstByEmail(email)).thenReturn(Optional.of(user));
        when(identifierCodeRepository.existsByCode(anyString())).thenReturn(false);

        // when
        authService.sendEmailLoginCode(email, "ko");

        // then
        verify(notificationService).sendEmailCode(eq(email), anyString(), eq("ko"));
        verify(identifierCodeRepository).save(any(IdentifierCode.class));
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 - 성공")
    void verifyEmailLoginCode_Success() {
        // given
        String email = "test@example.com";
        String code = "123456";
        User user = User.builder()
                .id(1L)
                .isVerifiedIdentity(false)
                .build();
        
        IdentifierCode identifierCode = new IdentifierCode();
        identifierCode.setUser(user);
        identifierCode.setCode(code);
        
        when(identifierCodeRepository.findTopByUserEmailOrderByIdDesc(email)).thenReturn(Optional.of(identifierCode));
        when(jwtTokenProvider.generateToken(eq(1L), eq(TokenType.REGISTER))).thenReturn("verified-token");

        // when
        LoginResponse response = authService.verifyEmailLoginCode(email, code);

        // then
        assertEquals("verified-token", response.registerToken());
        verify(jwtTokenProvider).generateToken(1L, TokenType.REGISTER);
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 - 실패 (잘못된 코드)")
    void verifyEmailLoginCode_WrongCode() {
        // given
        String email = "test@example.com";
        String code = "123456";
        
        IdentifierCode identifierCode = new IdentifierCode();
        identifierCode.setCode("654321"); // Different code
        
        when(identifierCodeRepository.findTopByUserEmailOrderByIdDesc(email)).thenReturn(Optional.of(identifierCode));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> authService.verifyEmailLoginCode(email, code));
    }
}

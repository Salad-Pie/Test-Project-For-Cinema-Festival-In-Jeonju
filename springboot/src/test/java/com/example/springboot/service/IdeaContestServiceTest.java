package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.springboot.domain.IdeaContest;
import com.example.springboot.domain.User;
import com.example.springboot.dto.IdeaContestResponse;
import com.example.springboot.repository.IdeaContestRepository;
import com.example.springboot.repository.UserRepository;
import com.example.springboot.security.JwtTokenProvider;
import com.example.springboot.security.TokenType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class IdeaContestServiceTest {

    private IdeaContestService ideaContestService;

    @Mock private IdeaContestRepository ideaContestRepository;
    @Mock private UserRepository userRepository;
    @Mock private S3UploadService s3UploadService;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private PointRewardService pointRewardService;

    @BeforeEach
    void setUp() {
        ideaContestService = new IdeaContestService(
                ideaContestRepository,
                userRepository,
                s3UploadService,
                jwtTokenProvider,
                pointRewardService
        );
    }

    @Test
    @DisplayName("아이디어 공모전 제출 성공")
    void create_Success() {
        // given
        String token = "valid-token";
        MultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> images = List.of(image);

        User user = User.builder().id(1L).build();

        when(jwtTokenProvider.extractTokenType(token)).thenReturn(TokenType.VERIFIED);
        when(jwtTokenProvider.extractUserId(token)).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        S3UploadService.UploadedImage uploadedImage = new S3UploadService.UploadedImage("key", 100L);
        when(s3UploadService.uploadMemoImage(any(MultipartFile.class))).thenReturn(uploadedImage);
        lenient().when(s3UploadService.createPresignedDownloadUrl(anyString())).thenReturn("http://download.url");

        IdeaContest savedContest = IdeaContest.builder()
                .id(10L)
                .user(user)
                .build();
        when(ideaContestRepository.save(any(IdeaContest.class))).thenReturn(savedContest);

        // when
        IdeaContestResponse response = ideaContestService.create(token, images);

        // then
        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals(1L, response.userId());
        verify(pointRewardService).earnActivityPoints(eq(1L), anyString());
    }

    @Test
    @DisplayName("이미지 없이 제출 시 예외 발생")
    void create_NoImages() {
        // given
        String token = "valid-token";
        List<MultipartFile> images = List.of(); // Empty

        // when & then
        assertThrows(IllegalArgumentException.class, () -> ideaContestService.create(token, images));
    }
}

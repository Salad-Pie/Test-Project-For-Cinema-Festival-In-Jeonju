package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.example.springboot.domain.PointReward;
import com.example.springboot.domain.User;
import com.example.springboot.dto.PointRankingResponse;
import com.example.springboot.repository.PointRewardRepository;
import com.example.springboot.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointRewardServiceTest {

    private PointRewardService pointRewardService;

    @Mock private PointRewardRepository pointRewardRepository;
    @Mock private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        pointRewardService = new PointRewardService(pointRewardRepository, userRepository);
    }

    @Test
    @DisplayName("활동 포인트 적립 성공")
    void earnActivityPoints() {
        // given
        Long userId = 1L;
        String reason = "테스트 적립";
        when(pointRewardRepository.findLastBalanceByUserId(userId)).thenReturn(Optional.of(10));

        // when
        int earned = pointRewardService.earnActivityPoints(userId, reason);

        // then
        assertTrue(earned >= 1 && earned <= 5);
        verify(pointRewardRepository).save(any(PointReward.class));
    }

    @Test
    @DisplayName("최근 랭킹 조회 및 정렬 확인")
    void getRecentRanking() {
        // given
        User user1 = User.builder().id(1L).email("user1@test.com").build();
        User user2 = User.builder().id(2L).email("user2@test.com").build();
        
        when(userRepository.findByCreatedAtAfter(any(OffsetDateTime.class))).thenReturn(List.of(user1, user2));
        when(pointRewardRepository.findLastBalanceByUserId(1L)).thenReturn(Optional.of(50));
        when(pointRewardRepository.findLastBalanceByUserId(2L)).thenReturn(Optional.of(100));

        // when
        List<PointRankingResponse> ranking = pointRewardService.getRecentRanking(2);

        // then
        assertEquals(2, ranking.size());
        assertEquals("user2@test.com", ranking.get(0).email()); // 점수 높은 유저가 1위
        assertEquals(1, ranking.get(0).rank());
        assertEquals(100, ranking.get(0).totalPoints());
        assertEquals(2, ranking.get(1).rank());
    }
}

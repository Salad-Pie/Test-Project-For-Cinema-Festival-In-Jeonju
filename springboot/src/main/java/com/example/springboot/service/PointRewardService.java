package com.example.springboot.service;

import com.example.springboot.domain.PointReward;
import com.example.springboot.dto.PointRankingResponse;
import com.example.springboot.repository.PointRewardRepository;
import com.example.springboot.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointRewardService {

    private final PointRewardRepository pointRewardRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public PointRewardService(PointRewardRepository pointRewardRepository, UserRepository userRepository) {
        this.pointRewardRepository = pointRewardRepository;
        this.userRepository = userRepository;
    }

    /**
     * 유저에게 활동 포인트를 적립합니다. (1~5점 랜덤)
     */
    @Transactional
    public int earnActivityPoints(Long userId, String reason) {
        int pointsToEarn = random.nextInt(5) + 1; // 1 ~ 5점 랜덤
        
        int currentBalance = pointRewardRepository.findLastBalanceByUserId(userId).orElse(0);
        int nextBalance = currentBalance + pointsToEarn;

        PointReward reward = new PointReward();
        reward.setUserId(userId);
        reward.setPointType("earn");
        reward.setPointsDelta(pointsToEarn);
        reward.setBalanceAfter(nextBalance);
        reward.setReasonText(reason);
        
        pointRewardRepository.save(reward);
        return pointsToEarn;
    }

    /**
     * 최근 N시간 내 가입한 유저들의 랭킹을 가져옵니다.
     */
    @Transactional(readOnly = true)
    public List<PointRankingResponse> getRecentRanking(int hours) {
        OffsetDateTime threshold = OffsetDateTime.now().minusHours(hours);
        List<com.example.springboot.domain.User> recentUsers = userRepository.findByCreatedAtAfter(threshold);

        List<PointRankingResponse> rankings = recentUsers.stream()
                .map(user -> {
                    int balance = pointRewardRepository.findLastBalanceByUserId(user.getId()).orElse(0);
                    return new PointRankingResponse(user.getEmail(), balance, 0);
                })
                .sorted(Comparator.comparingInt(PointRankingResponse::totalPoints).reversed())
                .collect(Collectors.toList());

        // 순위 매기기
        AtomicInteger ranker = new AtomicInteger(1);
        return rankings.stream()
                .map(r -> new PointRankingResponse(r.email(), r.totalPoints(), ranker.getAndIncrement()))
                .collect(Collectors.toList());
    }
}

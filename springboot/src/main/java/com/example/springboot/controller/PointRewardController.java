package com.example.springboot.controller;

import com.example.springboot.dto.PointRankingResponse;
import com.example.springboot.service.PointRewardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/point-reward")
@RequiredArgsConstructor
public class PointRewardController {

    private final PointRewardService pointRewardService;

    /**
     * 최근 가입 유저들의 포인트 랭킹 조회
     */
    @GetMapping("/ranking")
    public List<PointRankingResponse> getRanking(@RequestParam(defaultValue = "2") int hours) {
        return pointRewardService.getRecentRanking(hours);
    }
}

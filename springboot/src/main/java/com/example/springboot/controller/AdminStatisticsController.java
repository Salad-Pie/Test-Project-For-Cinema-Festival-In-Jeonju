package com.example.springboot.controller;

import com.example.springboot.dto.admin.AdminStatisticsResponse;
import com.example.springboot.service.AdminStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

    private final AdminStatisticsService statisticsService;

    public AdminStatisticsController(AdminStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public ResponseEntity<AdminStatisticsResponse> getStatistics() {
        // Security check is handled by the global SecurityConfig filter 
        // which verifies ADMIN role for /api/admin/** routes
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
}

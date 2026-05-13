package com.example.springboot.dto.admin;

import java.util.Map;
import java.util.List;

public record AdminStatisticsResponse(
    long totalUsers,
    long totalSignatures,
    long totalReservations,
    Map<String, Long> reservationsByType,
    Map<String, Long> userLocales,
    double averageOcrConfidence,
    List<DailyCount> dailyTrend
) {
    public record DailyCount(String date, long count) {}
}

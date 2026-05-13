package com.example.springboot.dto;

public record PointRankingResponse(
    String name,
    String nickname,
    int totalPoints,
    int rank
) {}

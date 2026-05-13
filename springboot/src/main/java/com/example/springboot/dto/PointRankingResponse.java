package com.example.springboot.dto;

public record PointRankingResponse(
    String email,
    int totalPoints,
    int rank
) {}

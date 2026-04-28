package com.example.springboot.dto;

import java.util.List;

public record IdeaContestResponse(
        Long id,
        Long userId,
        List<MemoImageResponse> images
) {
}

package com.example.springboot.dto;

public record MemoImageResponse(
        Long id,
        String originalFilename,
        String s3Key,
        String presignedDownloadUrl,
        Long fileSize
) {
}

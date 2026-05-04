package com.example.springboot.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signatures")
public class Signature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "original_s3_key", nullable = false, length = 500)
    private String originalS3Key;

    @Column(name = "original_file_size", nullable = false)
    private Long originalFileSize;

    @Column(name = "original_content_type", nullable = false, length = 50)
    private String originalContentType;

    @Lob
    private String recognizedText;

    @Enumerated(EnumType.STRING)
    @Column(name = "detected_language", nullable = false, length = 10)
    private SignatureLanguage detectedLanguage;

    @Column(name = "ocr_confidence")
    private Double ocrConfidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "ocr_provider", nullable = false, length = 30)
    private OcrProvider ocrProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "ocr_status", nullable = false, length = 20)
    private OcrStatus ocrStatus;

    @Column(name = "ocr_error_message", length = 500)
    private String ocrErrorMessage;

    @Column(name = "preprocessed_s3_key", length = 500)
    private String preprocessedS3Key;

    @Column(name = "ocr_raw_response_s3_key", length = 500)
    private String ocrRawResponseS3Key;

    @Column(name = "ocr_processed_at")
    private LocalDateTime ocrProcessedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getOriginalS3Key() {
        return originalS3Key;
    }

    public void setOriginalS3Key(String originalS3Key) {
        this.originalS3Key = originalS3Key;
    }

    public Long getOriginalFileSize() {
        return originalFileSize;
    }

    public void setOriginalFileSize(Long originalFileSize) {
        this.originalFileSize = originalFileSize;
    }

    public String getOriginalContentType() {
        return originalContentType;
    }

    public void setOriginalContentType(String originalContentType) {
        this.originalContentType = originalContentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getRecognizedText() {
        return recognizedText;
    }

    public void setRecognizedText(String recognizedText) {
        this.recognizedText = recognizedText;
    }

    public SignatureLanguage getDetectedLanguage() {
        return detectedLanguage;
    }

    public void setDetectedLanguage(SignatureLanguage detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }

    public Double getOcrConfidence() {
        return ocrConfidence;
    }

    public void setOcrConfidence(Double ocrConfidence) {
        this.ocrConfidence = ocrConfidence;
    }

    public OcrProvider getOcrProvider() {
        return ocrProvider;
    }

    public void setOcrProvider(OcrProvider ocrProvider) {
        this.ocrProvider = ocrProvider;
    }

    public OcrStatus getOcrStatus() {
        return ocrStatus;
    }

    public void setOcrStatus(OcrStatus ocrStatus) {
        this.ocrStatus = ocrStatus;
    }

    public String getOcrErrorMessage() {
        return ocrErrorMessage;
    }

    public void setOcrErrorMessage(String ocrErrorMessage) {
        this.ocrErrorMessage = ocrErrorMessage;
    }

    public String getPreprocessedS3Key() {
        return preprocessedS3Key;
    }

    public void setPreprocessedS3Key(String preprocessedS3Key) {
        this.preprocessedS3Key = preprocessedS3Key;
    }

    public String getOcrRawResponseS3Key() {
        return ocrRawResponseS3Key;
    }

    public void setOcrRawResponseS3Key(String ocrRawResponseS3Key) {
        this.ocrRawResponseS3Key = ocrRawResponseS3Key;
    }

    public LocalDateTime getOcrProcessedAt() {
        return ocrProcessedAt;
    }

    public void setOcrProcessedAt(LocalDateTime ocrProcessedAt) {
        this.ocrProcessedAt = ocrProcessedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

package com.example.springboot.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signatures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Signature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(lombok.AccessLevel.NONE)
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

    @Column(name = "original_name", length = 150)
    private String originalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "name_language", length = 20)
    private NameLanguage nameLanguage;

    @Lob
    @Column(name = "korean_text")
    private String koreanText;

    @Lob
    @Column(name = "korean_meaning_text")
    private String koreanMeaningText;

    @Column(name = "english_name", length = 100)
    private String englishName;

    @Column(name = "korean_name", length = 100)
    private String koreanName;

    @Enumerated(EnumType.STRING)
    @Column(name = "name_conversion_source", length = 30)
    private NameConversionSource nameConversionSource;

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
    @Setter(lombok.AccessLevel.NONE)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Setter(lombok.AccessLevel.NONE)
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
}

package com.example.springboot.service;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Service
public class S3UploadService {

    private final String bucket;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final Duration downloadUrlExpiration;

    public S3UploadService(
            @Value("${app.aws.s3.bucket:}") String bucket,
            @Value("${app.aws.s3.region:ap-northeast-2}") String region,
            @Value("${app.aws.s3.presigned-download-expiration-minutes:10}") long downloadExpirationMinutes
    ) {
        this.bucket = bucket;
        Region awsRegion = Region.of(region);
        this.s3Client = S3Client.builder().region(awsRegion).build();
        this.s3Presigner = S3Presigner.builder().region(awsRegion).build();
        this.downloadUrlExpiration = Duration.ofMinutes(downloadExpirationMinutes);
    }

    public String uploadMemoImage(MultipartFile file) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("AWS_S3_BUCKET is not configured.");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("image file is empty.");
        }

        String originalFilename = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        String key = "idea-memo/" + UUID.randomUUID() + "-" + originalFilename;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return key;
        } catch (IOException e) {
            throw new IllegalStateException("failed to read image bytes.", e);
        }
    }

    public String createPresignedDownloadUrl(String key) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("AWS_S3_BUCKET is not configured.");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("s3 key is required.");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(downloadUrlExpiration)
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}

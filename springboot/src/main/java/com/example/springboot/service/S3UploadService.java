package com.example.springboot.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.time.Duration;
import java.util.UUID;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import com.example.springboot.exception.BusinessException;
import com.example.springboot.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(S3UploadService.class);
    private static final String WEBP_CONTENT_TYPE = "image/webp";
    private static final float WEBP_COMPRESSION_QUALITY = 0.8f;

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

    public UploadedImage uploadMemoImage(MultipartFile file) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("AWS_S3_BUCKET is not configured.");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }

        String originalFilename = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        validateSupportedImage(file, originalFilename);
        String key = "idea-memo/" + UUID.randomUUID() + "-" + toWebpFilename(originalFilename);

        try {
            byte[] webpBytes = convertToWebp(file);
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(WEBP_CONTENT_TYPE)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(webpBytes));
            double compressionRatio = file.getSize() == 0 ? 0 : (double) webpBytes.length / file.getSize();
            double reductionPercent = file.getSize() == 0 ? 0 : (1 - compressionRatio) * 100;
            log.info(
                    "Memo image converted and uploaded. originalFilename={} originalSize={} webpSize={} compressionRatio={} reductionPercent={} s3Key={}",
                    originalFilename,
                    file.getSize(),
                    webpBytes.length,
                    String.format("%.4f", compressionRatio),
                    String.format("%.2f", reductionPercent),
                    key
            );
            return new UploadedImage(key, (long) webpBytes.length);
        } catch (IOException e) {
            throw new IllegalStateException("failed to convert image to webp.", e);
        }
    }

    private void validateSupportedImage(MultipartFile file, String originalFilename) {
        String extension = getExtension(originalFilename);
        if (!extension.equals("jpg") && !extension.equals("jpeg") && !extension.equals("png")) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private String getExtension(String filename) {
        int extensionIndex = filename.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
    }

    private byte[] convertToWebp(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(WEBP_CONTENT_TYPE);
        if (!writers.hasNext()) {
            throw new IllegalStateException("webp image writer is not available.");
        }

        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(imageOutput);

            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                String[] compressionTypes = writeParam.getCompressionTypes();
                if (compressionTypes != null && compressionTypes.length > 0) {
                    writeParam.setCompressionType(compressionTypes[0]);
                }
                writeParam.setCompressionQuality(WEBP_COMPRESSION_QUALITY);
            }

            writer.write(null, new IIOImage(image, null, null), writeParam);
            imageOutput.flush();
            return output.toByteArray();
        } finally {
            writer.dispose();
        }
    }

    private String toWebpFilename(String originalFilename) {
        String normalized = originalFilename.replace('\\', '-').replace('/', '-');
        int extensionIndex = normalized.lastIndexOf('.');
        String baseName = extensionIndex > 0 ? normalized.substring(0, extensionIndex) : normalized;
        if (baseName.isBlank()) {
            baseName = "memo-image";
        }
        return baseName + ".webp";
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

    public record UploadedImage(String s3Key, Long fileSize) {
    }
}

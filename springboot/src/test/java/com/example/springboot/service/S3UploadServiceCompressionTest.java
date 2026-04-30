package com.example.springboot.service;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class S3UploadServiceCompressionTest {

    private static final List<Path> SAMPLE_IMAGES = List.of(
            Path.of("C:/Users/dldbs/Pictures/Screenshots/zdo sp포인트.png"),
            Path.of("C:/Users/dldbs/Pictures/Screenshots/스크린샷 2026-03-26 143029.png"),
            Path.of("C:/Users/dldbs/Pictures/Screenshots/스크린샷 2026-03-26 143441.png"),
            Path.of("C:/Users/dldbs/Pictures/Screenshots/스크린샷 2026-04-02 123059.png"),
            Path.of("C:/Users/dldbs/Pictures/Screenshots/스크린샷 2026-04-02 132711.png"),
            Path.of("C:/Users/dldbs/Pictures/Screenshots/스크린샷 2026-04-04 095137.png"),
            Path.of("C:/Users/dldbs/Pictures/Screenshots/스크린샷 2026-04-04 203524.png"),
            Path.of("C:/Users/dldbs/Pictures/Screenshots/스크린샷 2026-03-26 162748.png"),
            Path.of("C:/Users/dldbs/Pictures/Screenshots/스크린샷 2026-04-04 200804.png")
    );

    @Test
    void measureWebpCompressionRatioForLocalSampleImages() throws Exception {
        assumeTrue(SAMPLE_IMAGES.stream().allMatch(Files::exists), "local sample images are not available.");

        S3UploadService s3UploadService = new S3UploadService("dummy-bucket", "ap-northeast-2", 10);
        Method convertToWebp = S3UploadService.class.getDeclaredMethod("convertToWebp", org.springframework.web.multipart.MultipartFile.class);
        convertToWebp.setAccessible(true);

        System.out.println("filename\toriginalBytes\twebpBytes\tcompressionRatio\treductionPercent");
        for (Path imagePath : SAMPLE_IMAGES) {
            byte[] originalBytes = Files.readAllBytes(imagePath);
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "image",
                    imagePath.getFileName().toString(),
                    "image/png",
                    originalBytes
            );

            byte[] webpBytes = (byte[]) convertToWebp.invoke(s3UploadService, multipartFile);
            double compressionRatio = (double) webpBytes.length / originalBytes.length;
            double reductionPercent = (1 - compressionRatio) * 100;

            System.out.printf(
                    "%s\t%d\t%d\t%.4f\t%.2f%%%n",
                    imagePath.getFileName(),
                    originalBytes.length,
                    webpBytes.length,
                    compressionRatio,
                    reductionPercent
            );
        }
    }
}

package com.example.springboot.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CertificatePdfServiceTest {

    @Test
    void renderKoreanCalligraphyCertificateKeepsConsistentNameBoxes() throws Exception {
        SignatureImageService signatureImageService = new SignatureImageService();
        CertificatePdfService certificatePdfService = new CertificatePdfService(signatureImageService);

        byte[] pdf = certificatePdfService.renderKoreanCalligraphyCertificate(
                "ALEXANDER MICHAEL JOHNSON",
                "김도윤",
                null
        );

        Path outputDirectory = Path.of("build", "generated-samples");
        Files.createDirectories(outputDirectory);
        Path output = outputDirectory.resolve("Korean Calligraphy Experience Certificate_consistent_sample.pdf");
        Files.write(output, pdf);

        assertThat(pdf).startsWith("%PDF".getBytes());
        assertThat(Files.size(output)).isGreaterThan(0);
    }
}

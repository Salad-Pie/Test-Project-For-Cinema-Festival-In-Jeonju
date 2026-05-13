package com.example.springboot.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CertificatePdfSample01ExportTest {

    @Test
    void exportCertSample01ToDownloads() throws Exception {
        SignatureImageService signatureImageService =
                new SignatureImageService(new BrowserCalligraphyRenderService());
        CertificatePdfService certificatePdfService = new CertificatePdfService(signatureImageService);

        String koreanName = "\uC544\uB2C8\uD0C0 \uC548\uB2E4\uC57C\uB2C8";
        byte[] signaturePng = signatureImageService.renderYangjaeMaehwaSignatureText(
                koreanName,
                660,
                304
        );
        byte[] pdf = certificatePdfService.renderKoreanCalligraphyCertificate(
                "ANITA ANDAYANI",
                koreanName,
                signaturePng
        );

        Path output = Path.of(System.getProperty("user.home"), "Downloads", "cert_sample_01.pdf");
        Files.write(output, pdf);

        assertThat(signaturePng).startsWith(new byte[]{(byte) 0x89, 'P', 'N', 'G'});
        assertThat(pdf).startsWith("%PDF".getBytes());
        assertThat(Files.size(output)).isGreaterThan(0L);
    }
}

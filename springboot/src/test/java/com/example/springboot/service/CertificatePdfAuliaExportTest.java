package com.example.springboot.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CertificatePdfAuliaExportTest {

    @Test
    void exportAuliaCertificateToDownloadsCertFolder() throws Exception {
        SignatureImageService signatureImageService =
                new SignatureImageService(new BrowserCalligraphyRenderService());
        CertificatePdfService certificatePdfService = new CertificatePdfService(signatureImageService);

        String englishName = "AULIA ENJELINA RAUFIKA SUNARYO";
        String koreanName = "\uC544\uC6B8\uB9AC\uC544 \uC5D4\uC824\uB9AC\uB098 \uB77C\uC6B0\uD53C\uCE74 \uC218\uB098\uB974\uC694";

        byte[] signaturePng = signatureImageService.renderYangjaeMaehwaSignatureText(
                koreanName,
                1100,
                304
        );
        byte[] pdf = certificatePdfService.renderKoreanCalligraphyCertificate(
                englishName,
                koreanName,
                signaturePng
        );

        Path outputDirectory = Path.of(System.getProperty("user.home"), "Downloads", "cert");
        Files.createDirectories(outputDirectory);
        Path output = outputDirectory.resolve("AULIA_ENJELINA_RAUFIKA_SUNARYO.pdf");
        Files.write(output, pdf);

        assertThat(signaturePng).startsWith(new byte[]{(byte) 0x89, 'P', 'N', 'G'});
        assertThat(pdf).startsWith("%PDF".getBytes());
        assertThat(Files.size(output)).isGreaterThan(0L);
    }
}

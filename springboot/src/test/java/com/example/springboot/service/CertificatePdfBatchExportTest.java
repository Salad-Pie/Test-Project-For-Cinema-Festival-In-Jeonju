package com.example.springboot.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CertificatePdfBatchExportTest {

    @Test
    void exportRequestedCertificatesToDownloadsCertFolder() throws Exception {
        SignatureImageService signatureImageService =
                new SignatureImageService(new BrowserCalligraphyRenderService());
        CertificatePdfService certificatePdfService = new CertificatePdfService(signatureImageService);

        Map<String, String> names = new LinkedHashMap<>();
        names.put("ANITA ANDAYANI", "\uC544\uB2C8\uD0C0 \uC548\uB2E4\uC57C\uB2C8");
        names.put("LUKFINTIA FILIA", "\uB8E8\uD06C\uD540\uD2F0\uC544 \uD544\uB9AC\uC544");
        names.put("RAHMAT MUTAZIM EKA PUTRA", "\uB77C\uD750\uB9DB \uBB34\uD0C0\uC9D0 \uC5D0\uCE74 \uD478\uD2B8\uB77C");
        names.put("SILVANA RESKI MULIAWAN", "\uC2E4\uBC14\uB098 \uB808\uC2A4\uD0A4 \uBB3C\uB9AC\uC544\uC644");
        names.put("MARWIYA ARIFUDDIN TIMBANG", "\uB9C8\uB974\uC704\uC57C \uC544\uB9AC\uD478\uB518 \uD300\uBC29");
        names.put("MAYRIDA NUDE HAMMA", "\uB9C8\uC774\uB9AC\uB2E4 \uB204\uB370 \uD568\uB9C8");
        names.put("SUCI PRATIWI MANGANTAR", "\uC218\uCE58 \uD504\uB77C\uD2F0\uC704 \uB9DD\uC548\uD0C0\uB974");
        names.put("EVI SULTRIANA ZACHRI", "\uC5D0\uBE44 \uC220\uD2B8\uB9AC\uC544\uB098 \uC790\uD06C\uB9AC");
        names.put("LENDA LIEM", "\uB80C\uB2E4 \uB9AC\uC5E0");
        names.put("AULIA ENJELINA RAUFIKA SUNARYO", "\uC544\uC6B8\uB9AC\uC544 \uC5D4\uC824\uB9AC\uB098 \uB77C\uC6B0\uD53C\uCE74 \uC218\uB098\uB974\uC694");
        names.put("YUNITA MANDRASARI", "\uC720\uB2C8\uD0C0 \uB9CC\uB4DC\uB77C\uC0AC\uB9AC");
        names.put("YULIATI", "\uC728\uB9AC\uC544\uD2F0");
        names.put("NANIK HARIANI", "\uB098\uB2C9 \uD558\uB9AC\uC544\uB2C8");
        names.put("RIVANA AMELIA FIONITA", "\uB9AC\uBC14\uB098 \uC544\uBA5C\uB9AC\uC544 \uD53C\uC624\uB2C8\uD0C0");
        names.put("HADI HANDRIAN", "\uD558\uB514 \uD55C\uB4DC\uB9AC\uC548");

        Path outputDirectory = Path.of(System.getProperty("user.home"), "Downloads", "cert");
        Files.createDirectories(outputDirectory);

        for (Map.Entry<String, String> entry : names.entrySet()) {
            byte[] signaturePng = signatureImageService.renderYangjaeMaehwaSignatureText(
                    entry.getValue(),
                    1440,
                    484
            );
            byte[] pdf = certificatePdfService.renderKoreanCalligraphyCertificate(
                    entry.getKey(),
                    entry.getValue(),
                    signaturePng
            );

            String safeFileName = entry.getKey().replaceAll("[^A-Z0-9]+", "_").replaceAll("^_+|_+$", "");
            Path output = outputDirectory.resolve(safeFileName + ".pdf");
            Files.write(output, pdf);

            assertThat(signaturePng).startsWith(new byte[]{(byte) 0x89, 'P', 'N', 'G'});
            assertThat(pdf).startsWith("%PDF".getBytes());
            assertThat(Files.size(output)).isGreaterThan(0L);
        }
    }
}

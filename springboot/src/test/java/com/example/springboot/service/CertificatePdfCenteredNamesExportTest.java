package com.example.springboot.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class CertificatePdfCenteredNamesExportTest {

    @Test
    void exportCenteredSignatureCertificates() throws Exception {
        SignatureImageService signatureImageService =
                new SignatureImageService(new BrowserCalligraphyRenderService());
        CertificatePdfService certificatePdfService = new CertificatePdfService(signatureImageService);

        Map<String, String> names = new LinkedHashMap<>();
        names.put("LENDA LIEM", "\uB80C\uB2E4 \uB9AC\uC5E0");
        names.put("YULIATI", "\uC728\uB9AC\uC544\uD2F0");

        Path outputDirectory = Path.of(System.getProperty("user.home"), "Downloads", "cert");
        Files.createDirectories(outputDirectory);

        for (Map.Entry<String, String> entry : names.entrySet()) {
            byte[] signaturePng = signatureImageService.renderYangjaeMaehwaSignatureText(
                    entry.getValue(),
                    1440,
                    484
            );
            byte[] centeredSignaturePng = centerInkBounds(signaturePng);
            byte[] pdf = certificatePdfService.renderKoreanCalligraphyCertificate(
                    entry.getKey(),
                    entry.getValue(),
                    centeredSignaturePng
            );

            String safeFileName = entry.getKey().replaceAll("[^A-Z0-9]+", "_").replaceAll("^_+|_+$", "");
            Path output = outputDirectory.resolve(safeFileName + ".pdf");
            Files.write(output, pdf);

            assertThat(centeredSignaturePng).startsWith(new byte[]{(byte) 0x89, 'P', 'N', 'G'});
            assertThat(pdf).startsWith("%PDF".getBytes());
            assertThat(Files.size(output)).isGreaterThan(0L);
        }
    }

    private byte[] centerInkBounds(byte[] sourcePng) throws Exception {
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(sourcePng));
        assertThat(source).isNotNull();

        int minX = source.getWidth();
        int minY = source.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int argb = source.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xff;
                int red = (argb >>> 16) & 0xff;
                int green = (argb >>> 8) & 0xff;
                int blue = argb & 0xff;
                boolean isInkPixel = alpha > 16 && (red < 245 || green < 245 || blue < 245);
                if (!isInkPixel) {
                    continue;
                }
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }

        assertThat(maxX).isGreaterThan(minX);
        assertThat(maxY).isGreaterThan(minY);

        int inkWidth = maxX - minX + 1;
        int inkHeight = maxY - minY + 1;
        int targetX = Math.max(0, (source.getWidth() - inkWidth) / 2);
        int targetY = Math.max(0, (source.getHeight() - inkHeight) / 2);

        BufferedImage centered = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = centered.createGraphics();
        try {
            graphics.drawImage(
                    source,
                    targetX,
                    targetY,
                    targetX + inkWidth,
                    targetY + inkHeight,
                    minX,
                    minY,
                    maxX + 1,
                    maxY + 1,
                    null
            );
        } finally {
            graphics.dispose();
        }

        clearOuterEdgeArtifacts(centered, 6);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(centered, "png", output);
        return output.toByteArray();
    }

    private void clearOuterEdgeArtifacts(BufferedImage image, int thickness) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                boolean isOuterEdge =
                        x < thickness
                                || y < thickness
                                || x >= image.getWidth() - thickness
                                || y >= image.getHeight() - thickness;
                if (!isOuterEdge) {
                    continue;
                }
                image.setRGB(x, y, 0x00000000);
            }
        }
    }
}

package com.example.springboot.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;

class CertificatePdfSample001ExportTest {

    private static final String TEMPLATE_PATH = "/certificates/Certification_Sample.pdf";
    private static final float SIGNATURE_X = 125f;
    private static final float SIGNATURE_Y = 360f;
    private static final float SIGNATURE_WIDTH = 360f;
    private static final float SIGNATURE_HEIGHT = 121f;

    @Test
    void exportSample001ToDownloads() throws Exception {
        SignatureImageService signatureImageService =
                new SignatureImageService(new BrowserCalligraphyRenderService());

        String koreanName = "\uC544\uC6B8\uB9AC\uC544 \uC5D4\uC824\uB9AC\uB098 \uB77C\uC6B0\uD53C\uCE74 \uC218\uB098\uB974\uC694";
        byte[] signaturePng = signatureImageService.renderYangjaeMaehwaSignatureText(
                koreanName,
                Math.round(SIGNATURE_WIDTH * 4),
                Math.round(SIGNATURE_HEIGHT * 4)
        );

        assertSignatureHasSafeMargin(signaturePng);

        byte[] pdf = renderSamplePdf(signaturePng);

        Path output = Path.of(System.getProperty("user.home"), "Downloads", "sample_001.pdf");
        Files.write(output, pdf);

        assertThat(signaturePng).startsWith(new byte[]{(byte) 0x89, 'P', 'N', 'G'});
        assertThat(pdf).startsWith("%PDF".getBytes());
        assertThat(Files.size(output)).isGreaterThan(0L);
    }

    private byte[] renderSamplePdf(byte[] signaturePng) throws Exception {
        try (InputStream inputStream = CertificatePdfSample001ExportTest.class.getResourceAsStream(TEMPLATE_PATH)) {
            assertThat(inputStream).isNotNull();
            try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes());
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                PDPage page = document.getPage(0);
                PDImageXObject signatureXObject = PDImageXObject.createFromByteArray(document, signaturePng, "sample-signature");

                try (PDPageContentStream contentStream = new PDPageContentStream(
                        document,
                        page,
                        PDPageContentStream.AppendMode.APPEND,
                        true,
                        true
                )) {
                    drawImageFit(contentStream, signatureXObject, SIGNATURE_X, SIGNATURE_Y, SIGNATURE_WIDTH, SIGNATURE_HEIGHT);
                }

                document.save(output);
                return output.toByteArray();
            }
        }
    }

    private void drawImageFit(
            PDPageContentStream contentStream,
            PDImageXObject image,
            float x,
            float y,
            float width,
            float height
    ) throws Exception {
        float imageRatio = (float) image.getWidth() / image.getHeight();
        float boxRatio = width / height;
        float drawWidth = width;
        float drawHeight = height;
        if (imageRatio > boxRatio) {
            drawHeight = drawWidth / imageRatio;
        } else {
            drawWidth = drawHeight * imageRatio;
        }
        float drawX = x + (width - drawWidth) / 2f;
        float drawY = y + (height - drawHeight) / 2f;
        contentStream.drawImage(image, drawX, drawY, drawWidth, drawHeight);
    }

    private void assertSignatureHasSafeMargin(byte[] signaturePng) throws Exception {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(signaturePng));
        assertThat(image).isNotNull();

        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xff;
                if (alpha == 0) {
                    continue;
                }
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

        int safetyMargin = 6;
        assertThat(minX).isGreaterThanOrEqualTo(safetyMargin);
        assertThat(minY).isGreaterThanOrEqualTo(safetyMargin);
        assertThat(maxX).isLessThanOrEqualTo(image.getWidth() - 1 - safetyMargin);
        assertThat(maxY).isLessThanOrEqualTo(image.getHeight() - 1 - safetyMargin);
    }
}

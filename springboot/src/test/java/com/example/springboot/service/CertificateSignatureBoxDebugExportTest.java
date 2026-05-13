package com.example.springboot.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

class CertificateSignatureBoxDebugExportTest {

    private static final String TEMPLATE_PATH = "/certificates/Certification_Sample.pdf";
    private static final float BOX_X = 140f;
    private static final float BOX_Y = 405f;
    private static final float BOX_WIDTH = 330f;
    private static final float BOX_HEIGHT = 76f;
    private static final float TICK_INTERVAL = 15f;

    @Test
    void exportSignatureBoxDebugPdfToDownloads() throws Exception {
        byte[] templateBytes = loadTemplateBytes();
        try (PDDocument document = Loader.loadPDF(templateBytes);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = document.getPage(0);
            PDType1Font labelFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            try (PDPageContentStream contentStream = new PDPageContentStream(
                    document,
                    page,
                    PDPageContentStream.AppendMode.APPEND,
                    true,
                    true
            )) {
                drawSignatureBox(contentStream);
                drawAxisTicks(contentStream, labelFont);
                drawCoordinateLabels(contentStream, labelFont, boldFont);
            }

            document.save(output);

            Path outputFile = Path.of(System.getProperty("user.home"), "Downloads", "cert_signature_box_debug_double_width_shifted_left_80.pdf");
            Files.write(outputFile, output.toByteArray());

            assertThat(output.toByteArray()).startsWith("%PDF".getBytes());
            assertThat(Files.size(outputFile)).isGreaterThan(0L);
        }
    }

    private byte[] loadTemplateBytes() throws Exception {
        try (InputStream inputStream = CertificateSignatureBoxDebugExportTest.class.getResourceAsStream(TEMPLATE_PATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("certificate template not found.");
            }
            return inputStream.readAllBytes();
        }
    }

    private void drawSignatureBox(PDPageContentStream contentStream) throws Exception {
        contentStream.saveGraphicsState();
        contentStream.setLineDashPattern(new float[]{5f, 3f}, 0f);
        contentStream.setLineWidth(1.2f);
        contentStream.setStrokingColor(new Color(220, 20, 60));
        contentStream.addRect(BOX_X, BOX_Y, BOX_WIDTH, BOX_HEIGHT);
        contentStream.stroke();
        contentStream.restoreGraphicsState();
    }

    private void drawAxisTicks(PDPageContentStream contentStream, PDType1Font labelFont) throws Exception {
        contentStream.saveGraphicsState();
        contentStream.setLineWidth(0.7f);
        contentStream.setStrokingColor(new Color(30, 30, 30));

        for (float offset = 0f; offset <= BOX_WIDTH + 0.1f; offset += TICK_INTERVAL) {
            float x = BOX_X + Math.min(offset, BOX_WIDTH);
            contentStream.moveTo(x, BOX_Y - 7f);
            contentStream.lineTo(x, BOX_Y + 7f);
            contentStream.stroke();
            drawText(contentStream, labelFont, 7f, x - 5f, BOX_Y - 18f, Integer.toString(Math.round(offset)));
        }

        contentStream.restoreGraphicsState();
    }

    private void drawCoordinateLabels(PDPageContentStream contentStream, PDType1Font labelFont, PDType1Font boldFont) throws Exception {
        drawText(contentStream, boldFont, 10f, BOX_X, BOX_Y + BOX_HEIGHT + 18f, "signature box (double width, shifted left 80)");
        drawText(contentStream, labelFont, 8f, BOX_X, BOX_Y + BOX_HEIGHT + 6f, "x=140, y=405, width=330, height=76");
        drawText(contentStream, labelFont, 8f, BOX_X - 28f, BOX_Y + BOX_HEIGHT - 2f, "y=481");
        drawText(contentStream, labelFont, 8f, BOX_X - 28f, BOX_Y - 2f, "y=405");
        drawText(contentStream, labelFont, 8f, BOX_X - 4f, BOX_Y - 30f, "x=140");
        drawText(contentStream, labelFont, 8f, BOX_X + BOX_WIDTH - 8f, BOX_Y - 30f, "x=470");
        drawText(contentStream, labelFont, 8f, BOX_X + BOX_WIDTH + 8f, BOX_Y + BOX_HEIGHT / 2f, "h=76");
        drawText(contentStream, labelFont, 8f, BOX_X + BOX_WIDTH / 2f - 18f, BOX_Y + BOX_HEIGHT + 30f, "w=330");
    }

    private void drawText(PDPageContentStream contentStream, PDType1Font font, float fontSize, float x, float y, String text) throws Exception {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }
}

package com.example.springboot.service;

import com.example.springboot.dto.CertificateSampleRequest;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

@Service
public class SignatureImageService {

    private static final String[] KOREAN_FONT_CANDIDATES = {
            "Noto Sans CJK KR",
            "Noto Sans KR",
            "NanumGothic",
            "Malgun Gothic",
            "Apple SD Gothic Neo",
            "Dialog"
    };

    public byte[] renderSignature(String text, String fontFamily, Integer fontSize, Integer width, Integer height) {
        String value = defaultText(text);
        int imageWidth = defaultNumber(width, 800);
        int imageHeight = defaultNumber(height, 240);
        int size = defaultNumber(fontSize, 88);

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            applyQuality(graphics);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, imageWidth, imageHeight);
            graphics.setColor(Color.BLACK);
            graphics.setFont(createFont(fontFamily, Font.PLAIN, size, value));

            FontMetrics metrics = graphics.getFontMetrics();
            int x = Math.max(24, (imageWidth - metrics.stringWidth(value)) / 2);
            int y = Math.max(metrics.getAscent() + 24, (imageHeight - metrics.getHeight()) / 2 + metrics.getAscent());
            graphics.drawString(value, x, y);
            return toPng(image);
        } finally {
            graphics.dispose();
        }
    }

    public byte[] renderCertificateSample(CertificateSampleRequest request, String defaultName, String defaultSignature) {
        int width = 1600;
        int height = 1131;
        String title = blankToDefault(request == null ? null : request.title(), "BackToScreen 참여 증명서");
        String name = blankToDefault(request == null ? null : request.name(), defaultName);
        String signature = blankToDefault(request == null ? null : request.signatureText(), defaultSignature);
        String fontFamily = request == null ? null : request.fontFamily();

        int nameX = defaultNumber(request == null ? null : request.nameX(), 700);
        int nameY = defaultNumber(request == null ? null : request.nameY(), 540);
        int signatureX = defaultNumber(request == null ? null : request.signatureX(), 1050);
        int signatureY = defaultNumber(request == null ? null : request.signatureY(), 830);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            applyQuality(graphics);
            drawCertificateBackground(graphics, width, height);

            graphics.setColor(new Color(24, 45, 79));
            graphics.setFont(createFont(null, Font.BOLD, 72, title));
            drawCentered(graphics, title, width / 2, 220);

            String description = "본인은 폐영화관 재생 프로젝트에 참여하였음을 증명합니다.";
            graphics.setFont(createFont(null, Font.PLAIN, 34, description));
            drawCentered(graphics, description, width / 2, 360);

            graphics.setFont(createFont(null, Font.BOLD, 52, "이름"));
            graphics.drawString("이름", 500, nameY);
            graphics.setFont(createFont(fontFamily, Font.PLAIN, 64, name));
            graphics.drawString(name, nameX, nameY);

            String issuedAt = "발급일: 2026.05.04";
            String place = "장소: 전주 영화관 재생 프로젝트 공간";
            graphics.setFont(createFont(null, Font.PLAIN, 34, issuedAt + place));
            graphics.drawString(issuedAt, 500, 700);
            graphics.drawString(place, 500, 760);

            graphics.setFont(createFont(fontFamily, Font.PLAIN, 64, signature));
            graphics.drawString(signature, signatureX, signatureY);
            graphics.setFont(createFont(null, Font.PLAIN, 26, "서명"));
            graphics.drawString("서명", signatureX, signatureY + 54);

            return toPng(image);
        } finally {
            graphics.dispose();
        }
    }

    private void drawCertificateBackground(Graphics2D graphics, int width, int height) {
        graphics.setColor(new Color(248, 251, 255));
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(new Color(168, 211, 239));
        graphics.setStroke(new BasicStroke(16));
        graphics.drawRoundRect(70, 70, width - 140, height - 140, 44, 44);
        graphics.setColor(new Color(88, 174, 226, 70));
        graphics.fillOval(-180, -140, 520, 520);
        graphics.fillOval(width - 320, height - 300, 520, 520);
    }

    private void drawCentered(Graphics2D graphics, String text, int centerX, int baselineY) {
        FontMetrics metrics = graphics.getFontMetrics();
        graphics.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }

    private void applyQuality(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private byte[] toPng(BufferedImage image) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("failed to render image.", e);
        }
    }

    private String defaultText(String value) {
        return blankToDefault(value, "서명");
    }

    private String blankToDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private Font createFont(String fontFamily, int style, int size, String sampleText) {
        return new Font(resolveDisplayableFont(fontFamily, sampleText), style, size);
    }

    private String resolveDisplayableFont(String preferredFont, String sampleText) {
        String preferred = blankToDefault(preferredFont, "");
        if (!preferred.isBlank()) {
            Font font = new Font(preferred, Font.PLAIN, 12);
            if (canDisplay(font, sampleText)) {
                return preferred;
            }
        }

        String koreanFont = findAvailableKoreanFont(sampleText);
        if (koreanFont != null) {
            return koreanFont;
        }
        return "Dialog";
    }

    private String findAvailableKoreanFont(String sampleText) {
        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String candidate : KOREAN_FONT_CANDIDATES) {
            for (String availableFont : availableFonts) {
                if (availableFont.equalsIgnoreCase(candidate) && canDisplay(new Font(availableFont, Font.PLAIN, 12), sampleText)) {
                    return availableFont;
                }
            }
        }
        for (String availableFont : availableFonts) {
            if (canDisplay(new Font(availableFont, Font.PLAIN, 12), sampleText)) {
                return availableFont;
            }
        }
        return null;
    }

    private boolean canDisplay(Font font, String sampleText) {
        if (sampleText == null || sampleText.isBlank()) {
            return true;
        }
        return font.canDisplayUpTo(sampleText) < 0;
    }

    private int defaultNumber(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }
}

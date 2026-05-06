package com.example.springboot.service;

import com.example.springboot.dto.CertificateSampleRequest;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

@Service
public class SignatureImageService {

    private static final String[] KOREAN_FONT_CANDIDATES = {
            "Nanum Brush Script",
            "Nanum Pen Script",
            "NanumMyeongjo",
            "Noto Serif CJK KR",
            "Noto Serif KR",
            "Noto Sans CJK KR",
            "Noto Sans KR",
            "NanumGothic",
            "Batang",
            "궁서체",
            "궁서",
            "Gungsuh",
            "GungsuhChe",
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

    public byte[] renderCertificateTextBox(
            String text,
            String fontFamily,
            int width,
            int height,
            int baseFontSize,
            int minFontSize,
            boolean uppercase
    ) {
        String value = defaultText(text);
        if (uppercase) {
            value = value.toUpperCase();
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            applyQuality(graphics);
            graphics.setColor(new Color(24, 45, 79));
            drawFittedCenteredText(graphics, value, fontFamily, width, height, baseFontSize, minFontSize);
            return toPng(image);
        } finally {
            graphics.dispose();
        }
    }

    public byte[] renderSampleOriginalSignature(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            applyQuality(graphics);
            graphics.setColor(new Color(18, 18, 18));
            graphics.setStroke(new BasicStroke(Math.max(6, width / 70), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D.Float signature = new Path2D.Float();
            signature.moveTo(width * 0.06f, height * 0.50f);
            signature.curveTo(width * 0.13f, height * 0.46f, width * 0.12f, height * 0.25f, width * 0.07f, height * 0.13f);
            signature.moveTo(width * 0.05f, height * 0.54f);
            signature.curveTo(width * 0.18f, height * 0.50f, width * 0.20f, height * 0.61f, width * 0.13f, height * 0.74f);
            signature.curveTo(width * 0.19f, height * 0.68f, width * 0.24f, height * 0.35f, width * 0.28f, height * 0.48f);
            signature.curveTo(width * 0.31f, height * 0.61f, width * 0.28f, height * 0.79f, width * 0.37f, height * 0.66f);
            signature.curveTo(width * 0.44f, height * 0.55f, width * 0.44f, height * 0.33f, width * 0.49f, height * 0.36f);
            signature.curveTo(width * 0.54f, height * 0.39f, width * 0.48f, height * 0.75f, width * 0.57f, height * 0.68f);
            signature.curveTo(width * 0.62f, height * 0.64f, width * 0.64f, height * 0.38f, width * 0.70f, height * 0.43f);
            signature.curveTo(width * 0.78f, height * 0.49f, width * 0.67f, height * 0.76f, width * 0.77f, height * 0.70f);
            signature.curveTo(width * 0.83f, height * 0.66f, width * 0.76f, height * 0.48f, width * 0.84f, height * 0.50f);
            graphics.draw(signature);

            graphics.setStroke(new BasicStroke(Math.max(3, width / 130), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Float underline = new Path2D.Float();
            underline.moveTo(width * 0.70f, height * 0.68f);
            underline.curveTo(width * 0.80f, height * 0.60f, width * 0.93f, height * 0.56f, width * 0.98f, height * 0.59f);
            graphics.draw(underline);
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

    private void drawFittedCenteredText(
            Graphics2D graphics,
            String text,
            String fontFamily,
            int width,
            int height,
            int baseFontSize,
            int minFontSize
    ) {
        String[] lines = new String[]{text};
        int size = Math.max(minFontSize, baseFontSize);
        Font font = createFont(fontFamily, Font.PLAIN, size, text);
        FontMetrics metrics = graphics.getFontMetrics(font);
        while (size > minFontSize && !fits(metrics, lines, width, height)) {
            size--;
            font = createFont(fontFamily, Font.PLAIN, size, text);
            metrics = graphics.getFontMetrics(font);
        }

        if (!fits(metrics, lines, width, height) && text.contains(" ")) {
            lines = splitIntoTwoLines(text);
            size = Math.max(minFontSize, baseFontSize);
            font = createFont(fontFamily, Font.PLAIN, size, text);
            metrics = graphics.getFontMetrics(font);
            while (size > minFontSize && !fits(metrics, lines, width, height)) {
                size--;
                font = createFont(fontFamily, Font.PLAIN, size, text);
                metrics = graphics.getFontMetrics(font);
            }
        }

        graphics.setFont(font);
        int lineHeight = metrics.getHeight();
        int totalHeight = lineHeight * lines.length;
        int baseline = (height - totalHeight) / 2 + metrics.getAscent();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int x = (width - metrics.stringWidth(line)) / 2;
            graphics.drawString(line, x, baseline + i * lineHeight);
        }
    }

    private boolean fits(FontMetrics metrics, String[] lines, int width, int height) {
        int allowedWidth = (int) (width * 0.92);
        int allowedHeight = (int) (height * 0.86);
        for (String line : lines) {
            if (metrics.stringWidth(line) > allowedWidth) {
                return false;
            }
        }
        return metrics.getHeight() * lines.length <= allowedHeight;
    }

    private String[] splitIntoTwoLines(String text) {
        String[] words = text.trim().split("\\s+");
        if (words.length <= 1) {
            return new String[]{text};
        }
        int splitIndex = words.length / 2;
        StringBuilder first = new StringBuilder();
        StringBuilder second = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i < splitIndex) {
                if (!first.isEmpty()) {
                    first.append(' ');
                }
                first.append(words[i]);
            } else {
                if (!second.isEmpty()) {
                    second.append(' ');
                }
                second.append(words[i]);
            }
        }
        return new String[]{first.toString(), second.toString()};
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
        String preferred = normalizeFontFamily(blankToDefault(preferredFont, ""));
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

    private String normalizeFontFamily(String fontFamily) {
        if (fontFamily == null || fontFamily.isBlank()) {
            return "";
        }
        return switch (fontFamily.trim()) {
            case "궁서", "궁서체" -> "Gungsuh";
            default -> fontFamily.trim();
        };
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

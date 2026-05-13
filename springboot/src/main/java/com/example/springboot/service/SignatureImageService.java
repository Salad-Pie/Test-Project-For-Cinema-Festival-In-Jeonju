package com.example.springboot.service;

import com.example.springboot.dto.CertificateSampleRequest;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class SignatureImageService {

    private static final Logger log = LoggerFactory.getLogger(SignatureImageService.class);
    private static final String BUNDLED_KOREAN_CALLIGRAPHY_FONT_PATH = "/fonts/GapyeongHanseokbongB.ttf";
    private static final String BUNDLED_KOREAN_SERIF_FONT_PATH = "/fonts/HANBatangB.ttf";
    private static final String BUNDLED_LATIN_SCRIPT_FONT_PATH = "/fonts/BRUSHSCI.TTF";
    private static final String BUNDLED_YANGJAE_MAEHWA_FONT_PATH = "fonts/YMAE07.TTF";

    private static final String[] KOREAN_FONT_CANDIDATES = {
            "Nanum Brush Script",
            "Nanum Pen Script",
            "Gapyeong Hansukbong Big Brush B",
            "GapyeongHanseokbongB",
            "Nanum Myeongjo",
            "Noto Serif CJK KR",
            "Noto Serif KR",
            "Noto Sans CJK KR",
            "Noto Sans KR",
            "Batang",
            "Gungsuh",
            "GungsuhChe",
            "Malgun Gothic",
            "Apple SD Gothic Neo",
            "Dialog"
    };

    private final BrowserCalligraphyRenderService browserCalligraphyRenderService;

    public SignatureImageService(BrowserCalligraphyRenderService browserCalligraphyRenderService) {
        this.browserCalligraphyRenderService = browserCalligraphyRenderService;
    }

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
        String title = blankToDefault(request == null ? null : request.title(), "BackToScreen \uCC38\uC5EC \uC99D\uBA85\uC11C");
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

            String description = "\uBCF8\uC778\uC740 \uD3D0\uC601\uD654\uAD00 \uC7AC\uC0DD \uD504\uB85C\uC81D\uD2B8\uC5D0 \uCC38\uC5EC\uD558\uC600\uC74C\uC744 \uC99D\uBA85\uD569\uB2C8\uB2E4.";
            graphics.setFont(createFont(null, Font.PLAIN, 34, description));
            drawCentered(graphics, description, width / 2, 360);

            graphics.setFont(createFont(null, Font.BOLD, 52, "\uC774\uB984"));
            graphics.drawString("\uC774\uB984", 500, nameY);
            graphics.setFont(createFont(fontFamily, Font.PLAIN, 64, name));
            graphics.drawString(name, nameX, nameY);

            String issuedAt = "\uBC1C\uAE09\uC77C: 2026.05.04";
            String place = "\uC7A5\uC18C: \uC804\uC8FC \uC601\uD654\uAD00 \uC7AC\uC0DD \uD504\uB85C\uC81D\uD2B8 \uACF5\uAC04";
            graphics.setFont(createFont(null, Font.PLAIN, 34, issuedAt + place));
            graphics.drawString(issuedAt, 500, 700);
            graphics.drawString(place, 500, 760);

            graphics.setFont(createFont(fontFamily, Font.PLAIN, 64, signature));
            graphics.drawString(signature, signatureX, signatureY);
            graphics.setFont(createFont(null, Font.PLAIN, 26, "\uC11C\uBA85"));
            graphics.drawString("\uC11C\uBA85", signatureX, signatureY + 54);

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

    public byte[] renderStrongCalligraphyText(String text, Integer width, Integer height) {
        String value = defaultText(text);
        int imageWidth = defaultNumber(width, 1400);
        int imageHeight = defaultNumber(height, 520);
        int maxAttempts = 3;

        // Retry the browser render a few times because Chromium startup can fail transiently in Docker.
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info(
                        "Trying browser calligraphy render. attempt={} textLength={} width={} height={}",
                        attempt,
                        value.length(),
                        imageWidth,
                        imageHeight
                );
                return browserCalligraphyRenderService.render(value, imageWidth, imageHeight);
            } catch (Exception e) {
                log.warn(
                        "Browser calligraphy render failed. attempt={} message={}",
                        attempt,
                        e.getMessage()
                );
            }
        }

        log.info(
                "Falling back to legacy calligraphy render after browser retries. textLength={} width={} height={}",
                value.length(),
                imageWidth,
                imageHeight
        );
        return renderStrongCalligraphyTextLegacy(value, imageWidth, imageHeight);
    }

    public byte[] renderYangjaeMaehwaSignatureText(String text, Integer width, Integer height) {
        String value = defaultText(text);
        int imageWidth = defaultNumber(width, 1400);
        int imageHeight = defaultNumber(height, 520);
        int fakeBoldCount = 1;

        log.info(
                "Rendering Yangjae Maehwa signature text. textLength={} width={} height={} fakeBoldCount={}",
                value.length(),
                imageWidth,
                imageHeight,
                fakeBoldCount
        );

          BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
          Graphics2D graphics = image.createGraphics();
          try {
              applyYangjaeQuality(graphics);
              graphics.setBackground(new Color(0, 0, 0, 0));
              graphics.clearRect(0, 0, imageWidth, imageHeight);
              graphics.setColor(new Color(5, 5, 5));

            FontRenderContext fontRenderContext = graphics.getFontRenderContext();
            double contentLeftPadding = imageWidth * 0.02d;
            double contentRightPadding = imageWidth * 0.02d;
            double availableWidth = Math.max(1d, imageWidth - contentLeftPadding - contentRightPadding);
            double availableHeight = imageHeight * 0.78d;
            int baseFontSize = findYangjaeFixedFontSize(imageHeight);
            Font font = resolveYangjaeSingleLineFont(graphics, value, baseFontSize, fontRenderContext, availableWidth, availableHeight);
            graphics.setFont(font);
            TextLayout textLayout = new TextLayout(value, font, fontRenderContext);
            Rectangle2D bounds = textLayout.getBounds();
            double scaleX = Math.min(1.10d, (availableWidth * 0.94d) / Math.max(1d, bounds.getWidth()));
            double baselineY = (imageHeight - bounds.getHeight()) / 2d - bounds.getY();

            log.info(
                    "Rendering Yangjae Maehwa signature layout. textLength={} availableWidth={} availableHeight={} fontSize={} boundsWidth={} boundsHeight={} scaleX={}",
                    value.length(),
                    availableWidth,
                    availableHeight,
                    font.getSize2D(),
                    bounds.getWidth(),
                    bounds.getHeight(),
                    scaleX
            );

            Graphics2D transformedGraphics = (Graphics2D) graphics.create();
            try {
                transformedGraphics.setRenderingHints(graphics.getRenderingHints());
                AffineTransform transform = new AffineTransform();
                transform.translate(contentLeftPadding - bounds.getX() * scaleX, baselineY);
                transform.scale(scaleX, 1d);
                transform.shear(-0.18d, 0d);
                transform.rotate(Math.toRadians(-1.2d), imageWidth / 2d, imageHeight / 2d);
                transformedGraphics.setTransform(transform);

                drawFakeBoldString(transformedGraphics, value, 0f, 0f, fakeBoldCount);
            } finally {
                transformedGraphics.dispose();
            }

            return toPng(image);
        } finally {
            graphics.dispose();
        }
    }

    private byte[] renderStrongCalligraphyTextLegacy(String value, int imageWidth, int imageHeight) {
        int supersampleScale = 3;
        int highWidth = imageWidth * supersampleScale;
        int highHeight = imageHeight * supersampleScale;
        int padding = Math.max(42, imageWidth / 24) * supersampleScale;

        log.info(
                "Rendering strong calligraphy text. textLength={} width={} height={} supersampleScale={}",
                value.length(),
                imageWidth,
                imageHeight,
                supersampleScale
        );

        BufferedImage textMask = new BufferedImage(highWidth, highHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = textMask.createGraphics();
        try {
            applyQuality(graphics);
            graphics.setColor(Color.BLACK);
            Font font = createFont(
                    "Gungsuh",
                    Font.PLAIN,
                    findMaxFontSize(graphics, value, highWidth - padding * 2, highHeight - padding * 2),
                    value
            );

            // Render on a larger canvas first so the later brush texture and downsample keep the edge detail.
            drawCalligraphyGlyphs(graphics, value, font, highWidth, highHeight, padding);
        } finally {
            graphics.dispose();
        }

        BufferedImage ink = dilateAlpha(textMask, 2);
        ink = applyInkTexture(ink);
        ink = addInkBleed(ink, 2);
        ink = addDryBrushBreaks(ink);
        ink = roughenInkEdge(ink, 26);
        ink = cropToInk(ink, 36 * supersampleScale);
        ink = downsampleInk(ink, imageWidth, imageHeight);
        return toPng(ink);
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

    private int findMaxFontSize(Graphics2D graphics, String text, int maxWidth, int maxHeight) {
        int size = Math.max(32, maxHeight);
        while (size > 32) {
            Font font = createFont("Gungsuh", Font.PLAIN, size, text);
            FontMetrics metrics = graphics.getFontMetrics(font);
            if (metrics.stringWidth(text) <= maxWidth && metrics.getHeight() <= maxHeight) {
                return size;
            }
            size -= 2;
        }
        return 32;
    }

    private void drawCalligraphyGlyphs(Graphics2D graphics, String text, Font font, int width, int height, int padding) {
        graphics.setFont(font);
        FontMetrics metrics = graphics.getFontMetrics(font);
        GlyphVector glyphVector = font.createGlyphVector(graphics.getFontRenderContext(), text);
        Rectangle2D visualBounds = glyphVector.getVisualBounds();
        double baseX = Math.max(padding, (width - visualBounds.getWidth()) / 2d - visualBounds.getX());
        double baselineY = (height - metrics.getHeight()) / 2d + metrics.getAscent();
        Random random = new Random(text.hashCode() * 31L + width * 17L + height);

        for (int index = 0; index < glyphVector.getNumGlyphs(); index++) {
            Shape glyph = glyphVector.getGlyphOutline(index, (float) baseX, (float) baselineY);
            Rectangle2D bounds = glyph.getBounds2D();
            if (bounds.isEmpty()) {
                continue;
            }

            double centerX = bounds.getCenterX();
            double centerY = bounds.getCenterY();
            double rotateRadians = Math.toRadians(randomBetween(random, -5.6, 5.6));
            double scaleX = randomBetween(random, 0.93, 1.08);
            double scaleY = randomBetween(random, 0.90, 1.18);
            double shearX = randomBetween(random, -0.09, 0.09);
            double shiftX = randomBetween(random, -18, 18);
            double shiftY = randomBetween(random, -12, 12);

            AffineTransform transform = new AffineTransform();
            transform.translate(shiftX, shiftY);
            transform.translate(centerX, centerY);
            transform.rotate(rotateRadians);
            transform.shear(shearX, 0);
            transform.scale(scaleX, scaleY);
            transform.translate(-centerX, -centerY);

            Shape transformedGlyph = transform.createTransformedShape(glyph);
            graphics.setColor(new Color(12, 12, 12, 245));
            graphics.fill(transformedGlyph);

            // Keep the center dense while the edge filters roughen only the outer contour later.
            graphics.setStroke(new BasicStroke(Math.max(3f, font.getSize2D() / 28f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.draw(transformedGlyph);
        }
    }

    private BufferedImage dilateAlpha(BufferedImage source, int radius) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int maxAlpha = 0;
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx < 0 || ny < 0 || nx >= source.getWidth() || ny >= source.getHeight()) {
                            continue;
                        }
                        int alpha = (source.getRGB(nx, ny) >>> 24) & 0xff;
                        if (alpha > maxAlpha) {
                            maxAlpha = alpha;
                        }
                    }
                }
                if (maxAlpha > 0) {
                    result.setRGB(x, y, (maxAlpha << 24));
                }
            }
        }
        return result;
    }

    private BufferedImage applyInkTexture(BufferedImage source) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int alpha = (source.getRGB(x, y) >>> 24) & 0xff;
                if (alpha == 0) {
                    continue;
                }

                double brushWave = 0.82d + 0.12d * Math.sin(x * 0.031d + y * 0.009d);
                double grain = 0.86d + 0.10d * Math.cos(y * 0.071d - x * 0.015d);
                double streak = 0.88d + 0.18d * Math.sin((x + y) * 0.019d);
                int texturedAlpha = (int) Math.round(alpha * brushWave * grain * streak);
                int finalAlpha = Math.max(0, Math.min(255, texturedAlpha));
                if (finalAlpha > 8) {
                    result.setRGB(x, y, ((finalAlpha & 0xff) << 24));
                }
            }
        }
        return result;
    }

    private BufferedImage roughenInkEdge(BufferedImage source, int strength) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Random random = new Random(20260506L);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int alpha = (source.getRGB(x, y) >>> 24) & 0xff;
                if (alpha == 0) {
                    continue;
                }
                int noise = random.nextInt(strength + 1);
                int edgePenalty = isEdgePixel(source, x, y) ? random.nextInt(strength * 5 + 1) : noise;
                int finalAlpha = Math.max(0, Math.min(255, alpha - edgePenalty));
                if (finalAlpha > 14) {
                    result.setRGB(x, y, (finalAlpha << 24));
                }
            }
        }
        return result;
    }

    private BufferedImage addDryBrushBreaks(BufferedImage source) {
        BufferedImage result = copyImage(source);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int alpha = (source.getRGB(x, y) >>> 24) & 0xff;
                if (alpha == 0 || !isEdgePixel(source, x, y)) {
                    continue;
                }

                double cut = Math.abs(Math.sin(x * 0.12d + y * 0.025d));
                if (cut > 0.92d) {
                    int reducedAlpha = Math.max(0, alpha - 120);
                    if (reducedAlpha > 10) {
                        result.setRGB(x, y, ((reducedAlpha & 0xff) << 24));
                    } else {
                        result.setRGB(x, y, 0x00000000);
                    }
                }
            }
        }
        return result;
    }

    private boolean isEdgePixel(BufferedImage source, int x, int y) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx < 0 || ny < 0 || nx >= source.getWidth() || ny >= source.getHeight()) {
                    return true;
                }
                int alpha = (source.getRGB(nx, ny) >>> 24) & 0xff;
                if (alpha == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private BufferedImage addInkBleed(BufferedImage source, int radius) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int alphaSum = 0;
                int count = 0;
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx < 0 || ny < 0 || nx >= source.getWidth() || ny >= source.getHeight()) {
                            continue;
                        }
                        alphaSum += (source.getRGB(nx, ny) >>> 24) & 0xff;
                        count++;
                    }
                }
                int originalAlpha = (source.getRGB(x, y) >>> 24) & 0xff;
                int bleedAlpha = count == 0 ? 0 : alphaSum / count;
                int alpha = Math.max(originalAlpha, Math.min(255, bleedAlpha / 2 + originalAlpha));
                if (alpha > 8) {
                    result.setRGB(x, y, (alpha << 24));
                }
            }
        }
        return result;
    }

    private BufferedImage cropToInk(BufferedImage source, int padding) {
        int minX = source.getWidth();
        int minY = source.getHeight();
        int maxX = 0;
        int maxY = 0;
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int alpha = (source.getRGB(x, y) >>> 24) & 0xff;
                if (alpha > 12) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (minX > maxX || minY > maxY) {
            return source;
        }
        minX = Math.max(0, minX - padding);
        minY = Math.max(0, minY - padding);
        maxX = Math.min(source.getWidth() - 1, maxX + padding);
        maxY = Math.min(source.getHeight() - 1, maxY + padding);
        return source.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private BufferedImage downsampleInk(BufferedImage source, int maxWidth, int maxHeight) {
        double scale = Math.min((double) maxWidth / source.getWidth(), (double) maxHeight / source.getHeight());
        scale = Math.min(scale, 1d);
        int targetWidth = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int targetHeight = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage result = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = result.createGraphics();
        try {
            applyQuality(graphics);
            graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return result;
    }

    private BufferedImage copyImage(BufferedImage source) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = result.getGraphics();
        try {
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return result;
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

    private void applyYangjaeQuality(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
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
        return blankToDefault(value, "\uC11C\uBA85");
    }

    private String blankToDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private Font loadYangjaeMaehwaFont(float fontSize) {
        try (InputStream inputStream = new ClassPathResource(BUNDLED_YANGJAE_MAEHWA_FONT_PATH).getInputStream()) {
            try {
                return Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(Font.PLAIN, fontSize);
            } catch (Exception first) {
                try (InputStream fallbackInputStream = new ClassPathResource(BUNDLED_YANGJAE_MAEHWA_FONT_PATH).getInputStream()) {
                    return Font.createFont(Font.TYPE1_FONT, fallbackInputStream).deriveFont(Font.PLAIN, fontSize);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("failed to load Yangjae Maehwa font.", e);
        }
    }

    private int findYangjaeFixedFontSize(int height) {
        return Math.max(64, (int) Math.round(height * 0.42d));
    }

    private Font resolveYangjaeSingleLineFont(
            Graphics2D graphics,
            String text,
            int baseFontSize,
            FontRenderContext fontRenderContext,
            double availableWidth,
            double availableHeight
    ) {
        for (int fontSize = baseFontSize; fontSize >= Math.max(48, baseFontSize - 2); fontSize--) {
            Font candidate = loadYangjaeMaehwaFont(fontSize);
            TextLayout layout = new TextLayout(text, candidate, fontRenderContext);
            Rectangle2D bounds = layout.getBounds();
            if (bounds.getHeight() <= availableHeight && bounds.getWidth() <= availableWidth) {
                return candidate;
            }
        }
        return loadYangjaeMaehwaFont(baseFontSize);
    }

    private void drawFakeBoldString(Graphics2D graphics, String text, float x, float y, int fakeBoldCount) {
        for (int dx = 0; dx <= fakeBoldCount; dx++) {
            for (int dy = 0; dy <= fakeBoldCount; dy++) {
                graphics.drawString(text, x + dx, y + dy);
            }
        }
    }

    private Font createFont(String fontFamily, int style, int size, String sampleText) {
        Font bundledFont = loadBundledFont(fontFamily, style, size, sampleText);
        if (bundledFont != null) {
            return bundledFont;
        }
        return new Font(resolveDisplayableFont(fontFamily, sampleText), style, size);
    }

    private Font loadBundledFont(String fontFamily, int style, int size, String sampleText) {
        String normalizedFontFamily = normalizeFontFamily(fontFamily);
        String resourcePath = resolveBundledFontResource(normalizedFontFamily, sampleText);
        if (resourcePath == null) {
            return null;
        }

        try (InputStream inputStream = SignatureImageService.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                log.warn("Bundled font resource not found. path={}", resourcePath);
                return null;
            }
            return Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(style, (float) size);
        } catch (Exception e) {
            log.warn("Failed to load bundled font. path={} message={}", resourcePath, e.getMessage());
            return null;
        }
    }

    private String resolveBundledFontResource(String normalizedFontFamily, String sampleText) {
        if (containsHangul(sampleText)) {
            if ("Gungsuh".equalsIgnoreCase(normalizedFontFamily) || normalizedFontFamily.isBlank()) {
                return BUNDLED_KOREAN_CALLIGRAPHY_FONT_PATH;
            }
            return BUNDLED_KOREAN_SERIF_FONT_PATH;
        }
        if ("Brush Script MT".equalsIgnoreCase(normalizedFontFamily) || "BRUSHSCI".equalsIgnoreCase(normalizedFontFamily)) {
            return BUNDLED_LATIN_SCRIPT_FONT_PATH;
        }
        return null;
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
        String trimmed = fontFamily.trim();
        String lower = trimmed.toLowerCase();
        if (lower.equals("gungsuh") || lower.equals("gungsuhche")) {
            return "Gungsuh";
        }
        return trimmed;
    }
    private boolean canDisplay(Font font, String sampleText) {
        if (sampleText == null || sampleText.isBlank()) {
            return true;
        }
        return font.canDisplayUpTo(sampleText) < 0;
    }
    private boolean containsHangul(String sampleText) {
        if (sampleText == null || sampleText.isBlank()) {
            return false;
        }
        return sampleText.codePoints().anyMatch(codePoint -> codePoint >= 0xAC00 && codePoint <= 0xD7A3);
    }

    private double randomBetween(Random random, double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    private int defaultNumber(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }
}


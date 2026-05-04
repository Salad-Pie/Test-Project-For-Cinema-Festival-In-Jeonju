package com.example.springboot.service;

import com.example.springboot.exception.BusinessException;
import com.example.springboot.exception.ErrorCode;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GoogleVisionOcrService {

    private final RestClient restClient;
    private final String apiKey;
    private final String endpoint;

    public GoogleVisionOcrService(
            @Value("${app.ocr.google.api-key:}") String apiKey,
            @Value("${app.ocr.google.endpoint:https://vision.googleapis.com/v1/images:annotate}") String endpoint
    ) {
        this.restClient = RestClient.builder().build();
        this.apiKey = apiKey;
        this.endpoint = endpoint;
    }

    public OcrResult extractSignatureText(MultipartFile image) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(ErrorCode.OCR_RECOGNITION_FAILED);
        }

        List<byte[]> candidates = createImageCandidates(image);
        List<OcrResult> results = new ArrayList<>();
        for (byte[] candidate : candidates) {
            results.add(callVision(candidate, "DOCUMENT_TEXT_DETECTION"));
            results.add(callVision(candidate, "TEXT_DETECTION"));
        }

        return results.stream()
                .filter(result -> result.text() != null && !result.text().isBlank())
                .max(Comparator.comparingDouble(OcrResult::score))
                .orElseThrow(() -> new BusinessException(ErrorCode.OCR_RECOGNITION_FAILED));
    }

    private OcrResult callVision(byte[] imageBytes, String featureType) {
        String base64 = Base64.getEncoder().encodeToString(imageBytes);

        Map<String, Object> request = Map.of(
                "requests", List.of(Map.of(
                        "image", Map.of("content", base64),
                        "features", List.of(Map.of("type", featureType))
                ))
        );

        try {
            Map<String, Object> response = restClient.post()
                    .uri(endpoint + "?key=" + apiKey)
                    .body(request)
                    .retrieve()
                    .body(Map.class);
            return parseResult(response);
        } catch (RuntimeException e) {
            return OcrResult.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private OcrResult parseResult(Map<String, Object> response) {
        if (response == null) {
            return OcrResult.empty();
        }

        Object responsesObj = response.get("responses");
        if (!(responsesObj instanceof List<?> responses) || responses.isEmpty()) {
            return OcrResult.empty();
        }

        Object first = responses.get(0);
        if (!(first instanceof Map<?, ?> firstMap)) {
            return OcrResult.empty();
        }

        Object fullTextObj = firstMap.get("fullTextAnnotation");
        if (fullTextObj instanceof Map<?, ?> fullTextMap) {
            Object text = fullTextMap.get("text");
            if (text != null) {
                return new OcrResult(String.valueOf(text).trim(), parseConfidence(fullTextMap));
            }
        }

        Object textAnnObj = firstMap.get("textAnnotations");
        if (textAnnObj instanceof List<?> textAnnotations && !textAnnotations.isEmpty()) {
            Object textFirst = textAnnotations.get(0);
            if (textFirst instanceof Map<?, ?> taMap && taMap.get("description") != null) {
                return new OcrResult(String.valueOf(taMap.get("description")).trim(), parseConfidence(taMap));
            }
        }

        return OcrResult.empty();
    }

    private double parseConfidence(Map<?, ?> map) {
        Object confidence = map.get("confidence");
        if (confidence instanceof Number number) {
            return number.doubleValue();
        }
        return 0.0;
    }

    private List<byte[]> createImageCandidates(MultipartFile image) {
        try {
            byte[] original = image.getBytes();
            BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
            if (bufferedImage == null) {
                throw new BusinessException(ErrorCode.INVALID_FILE);
            }

            return List.of(
                    original,
                    toPngBytes(upscaleWithWhiteBackground(bufferedImage, 2)),
                    toPngBytes(highContrast(upscaleWithWhiteBackground(bufferedImage, 2)))
            );
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }
    }

    private BufferedImage upscaleWithWhiteBackground(BufferedImage source, int scale) {
        int width = source.getWidth() * scale;
        int height = source.getHeight() * scale;
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, width, height, null);
            return target;
        } finally {
            graphics.dispose();
        }
    }

    private BufferedImage highContrast(BufferedImage source) {
        BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                Color color = new Color(source.getRGB(x, y));
                int gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                int binary = gray < 210 ? 0 : 255;
                target.setRGB(x, y, new Color(binary, binary, binary).getRGB());
            }
        }
        return target;
    }

    private byte[] toPngBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    public record OcrResult(String text, double confidence) {
        static OcrResult empty() {
            return new OcrResult("", 0.0);
        }

        double score() {
            String normalized = text == null ? "" : text.trim();
            long letterCount = normalized.chars().filter(Character::isLetter).count();
            long symbolCount = normalized.chars().filter(ch -> !Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch)).count();
            return confidence + (letterCount * 0.03) - (symbolCount * 0.02);
        }
    }
}

package com.example.springboot.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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

    public String extractTextFromDataUrl(String signatureDataUrl) {
        if (apiKey == null || apiKey.isBlank()) {
            return "";
        }

        String base64 = extractBase64(signatureDataUrl);

        Map<String, Object> request = Map.of(
                "requests", List.of(Map.of(
                        "image", Map.of("content", base64),
                        "features", List.of(Map.of("type", "DOCUMENT_TEXT_DETECTION"))
                ))
        );

        Map<String, Object> response = restClient.post()
                .uri(endpoint + "?key=" + apiKey)
                .body(request)
                .retrieve()
                .body(Map.class);

        return parseText(response);
    }

    @SuppressWarnings("unchecked")
    private String parseText(Map<String, Object> response) {
        if (response == null) {
            return "";
        }

        Object responsesObj = response.get("responses");
        if (!(responsesObj instanceof List<?> responses) || responses.isEmpty()) {
            return "";
        }

        Object first = responses.get(0);
        if (!(first instanceof Map<?, ?> firstMap)) {
            return "";
        }

        Object fullTextObj = firstMap.get("fullTextAnnotation");
        if (fullTextObj instanceof Map<?, ?> fullTextMap) {
            Object text = fullTextMap.get("text");
            if (text != null) {
                return String.valueOf(text).trim();
            }
        }

        Object textAnnObj = firstMap.get("textAnnotations");
        if (textAnnObj instanceof List<?> textAnnotations && !textAnnotations.isEmpty()) {
            Object textFirst = textAnnotations.get(0);
            if (textFirst instanceof Map<?, ?> taMap && taMap.get("description") != null) {
                return String.valueOf(taMap.get("description")).trim();
            }
        }

        return "";
    }

    private String extractBase64(String dataUrl) {
        if (dataUrl == null || dataUrl.isBlank()) {
            throw new IllegalArgumentException("signatureDataUrl is empty.");
        }

        int idx = dataUrl.indexOf(',');
        if (idx < 0 || idx == dataUrl.length() - 1) {
            throw new IllegalArgumentException("signatureDataUrl is invalid data URL.");
        }

        return dataUrl.substring(idx + 1);
    }
}

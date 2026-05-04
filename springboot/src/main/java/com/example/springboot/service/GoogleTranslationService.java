package com.example.springboot.service;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GoogleTranslationService {

    private static final Logger log = LoggerFactory.getLogger(GoogleTranslationService.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String endpoint;

    public GoogleTranslationService(
            @Value("${app.translation.google.api-key:${GOOGLE_TRANSLATE_API_KEY:}}") String apiKey,
            @Value("${app.translation.google.endpoint:https://translation.googleapis.com/language/translate/v2}") String endpoint
    ) {
        this.restClient = RestClient.builder().build();
        this.apiKey = apiKey;
        this.endpoint = endpoint;
    }

    public String translateEnglishToKorean(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        if (apiKey == null || apiKey.isBlank()) {
            log.info("Google Translation API key is not configured. meaningTranslationSkipped=true");
            return null;
        }

        Map<String, Object> request = Map.of(
                "q", text,
                "source", "en",
                "target", "ko",
                "format", "text"
        );

        try {
            Map<String, Object> response = restClient.post()
                    .uri(endpoint + "?key=" + apiKey)
                    .body(request)
                    .retrieve()
                    .body(Map.class);
            return parseTranslatedText(response);
        } catch (RuntimeException e) {
            log.info("Google Translation failed. message={}", e.getMessage());
            return null;
        }
    }

    private String parseTranslatedText(Map<String, Object> response) {
        if (response == null) {
            return null;
        }
        Object dataObj = response.get("data");
        if (!(dataObj instanceof Map<?, ?> data)) {
            return null;
        }
        Object translationsObj = data.get("translations");
        if (!(translationsObj instanceof List<?> translations) || translations.isEmpty()) {
            return null;
        }
        Object first = translations.get(0);
        if (first instanceof Map<?, ?> translation && translation.get("translatedText") != null) {
            return String.valueOf(translation.get("translatedText")).trim();
        }
        return null;
    }
}

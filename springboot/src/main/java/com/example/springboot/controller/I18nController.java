package com.example.springboot.controller;

import com.example.springboot.domain.I18nMessage;
import com.example.springboot.service.I18nService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/i18n")
public class I18nController {

    private final I18nService i18nService;

    public I18nController(I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @GetMapping("/messages/{locale}")
    public ResponseEntity<Map<String, String>> getMessages(@PathVariable String locale) {
        return ResponseEntity.ok(i18nService.getMessages(locale));
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    public ResponseEntity<List<I18nMessage>> getAllMessages() {
        return ResponseEntity.ok(i18nService.getAllMessages());
    }

    @PostMapping("/admin/update")
    public ResponseEntity<Void> updateMessage(@RequestBody Map<String, String> request) {
        String key = request.get("key");
        String locale = request.get("locale");
        String content = request.get("content");
        
        i18nService.updateMessage(key, locale, content);
        return ResponseEntity.ok().build();
    }
}

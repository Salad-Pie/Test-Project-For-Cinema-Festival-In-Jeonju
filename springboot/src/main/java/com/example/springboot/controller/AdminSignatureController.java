package com.example.springboot.controller;

import com.example.springboot.dto.AdminSignatureResponse;
import com.example.springboot.service.AdminSignatureService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/signatures")
public class AdminSignatureController {

    private final AdminSignatureService service;

    public AdminSignatureController(AdminSignatureService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AdminSignatureResponse>> signatures(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return ResponseEntity.ok(service.signatures(authorization));
    }

    @GetMapping("/low-confidence")
    public ResponseEntity<List<AdminSignatureResponse>> lowConfidenceSignatures(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "0.8") Double threshold
    ) {
        return ResponseEntity.ok(service.lowConfidenceSignatures(authorization, threshold));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AdminSignatureResponse> updateSignature(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body
    ) {
        String correctedText = body.get("correctedText");
        String status = body.get("status");
        return ResponseEntity.ok(service.updateSignature(authorization, id, correctedText, status));
    }
}

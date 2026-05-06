package com.example.springboot.controller;

import com.example.springboot.dto.AdminSignatureResponse;
import com.example.springboot.service.AdminSignatureService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

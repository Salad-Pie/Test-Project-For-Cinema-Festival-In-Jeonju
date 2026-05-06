package com.example.springboot.controller;

import com.example.springboot.dto.EndingCreditsEntryRequest;
import com.example.springboot.dto.EndingCreditsEntryResponse;
import com.example.springboot.service.CertificateDownloadService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ending-credits")
public class EndingCreditsController {

    private final CertificateDownloadService certificateDownloadService;

    public EndingCreditsController(CertificateDownloadService certificateDownloadService) {
        this.certificateDownloadService = certificateDownloadService;
    }

    @PostMapping("/lookup")
    public EndingCreditsEntryResponse lookup(@Valid @RequestBody EndingCreditsEntryRequest request) {
        return certificateDownloadService.lookupEndingCreditsByCode(request.code());
    }
}

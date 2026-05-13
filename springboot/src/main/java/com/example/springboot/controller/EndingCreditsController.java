package com.example.springboot.controller;

import com.example.springboot.dto.EndingCreditsEntryRequest;
import com.example.springboot.dto.EndingCreditsEntryResponse;
import com.example.springboot.dto.RecentSignatureResponse;
import com.example.springboot.service.CertificateDownloadService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/recent")
    public List<RecentSignatureResponse> recent(@RequestParam(defaultValue = "30") int minutes) {
        return certificateDownloadService.getRecentSignaturesForEndingCredits(minutes);
    }
}

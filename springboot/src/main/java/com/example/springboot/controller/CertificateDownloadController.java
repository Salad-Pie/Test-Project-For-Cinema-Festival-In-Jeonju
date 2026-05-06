package com.example.springboot.controller;

import com.example.springboot.dto.CertificateDownloadCodeRequest;
import com.example.springboot.service.CertificateDownloadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/certificate-download")
public class CertificateDownloadController {

    private final CertificateDownloadService certificateDownloadService;

    public CertificateDownloadController(CertificateDownloadService certificateDownloadService) {
        this.certificateDownloadService = certificateDownloadService;
    }

    @PostMapping(value = "/signature-image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> downloadSignatureImage(@Valid @RequestBody CertificateDownloadCodeRequest request) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"signature-image.png\"")
                .body(certificateDownloadService.renderSignatureImageByCode(request.code()));
    }

    @PostMapping(value = "/certificate-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadCertificatePdf(@Valid @RequestBody CertificateDownloadCodeRequest request) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificate.pdf\"")
                .body(certificateDownloadService.renderCertificatePdfByCode(request.code()));
    }
}

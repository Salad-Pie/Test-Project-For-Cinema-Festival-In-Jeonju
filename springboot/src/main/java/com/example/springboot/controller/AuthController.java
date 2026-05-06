package com.example.springboot.controller;

import com.example.springboot.dto.EmailLoginRequest;
import com.example.springboot.dto.CertificateSampleRequest;
import com.example.springboot.dto.EmailCodeSendRequest;
import com.example.springboot.dto.EmailCodeVerifyRequest;
import com.example.springboot.dto.LoginResponse;
import com.example.springboot.dto.SignatureRenderRequest;
import com.example.springboot.dto.SignatureResponse;
import com.example.springboot.dto.VerifyCodeRequest;
import com.example.springboot.dto.VerifyResponse;
import com.example.springboot.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/email")
    public ResponseEntity<LoginResponse> loginEmail(@Valid @RequestBody EmailLoginRequest request) {
        return ResponseEntity.ok(authService.loginEmail(request));
    }

    @PostMapping("/login/email/send-code")
    public ResponseEntity<Void> sendEmailCode(@Valid @RequestBody EmailCodeSendRequest request) {
        authService.sendEmailLoginCode(request.email(), request.language());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login/email/verify-code")
    public ResponseEntity<LoginResponse> verifyEmailCode(@Valid @RequestBody EmailCodeVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyEmailLoginCode(request.email(), request.code()));
    }

    @PostMapping("/login/email/identifier")
    public ResponseEntity<LoginResponse> loginEmailWithIdentifier(@Valid @RequestBody EmailCodeVerifyRequest request) {
        return ResponseEntity.ok(authService.loginEmailWithIdentifier(request.email(), request.code()));
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyResponse> verify(@Valid @RequestBody VerifyCodeRequest request) {
        return ResponseEntity.ok(authService.verify(request.token(), request.code()));
    }

    @PostMapping(value = "/signature", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SignatureResponse> saveSignature(
            @RequestHeader("Authorization") String authorization,
            @RequestPart("signatureImage") MultipartFile signatureImage,
            @RequestPart(value = "nameLanguage", required = false) String nameLanguage,
            @RequestPart(value = "koreanName", required = false) String koreanName
    ) {
        String token = authorization.replace("Bearer ", "");
        return ResponseEntity.ok(authService.saveSignature(token, signatureImage, nameLanguage, koreanName));
    }

    @PostMapping(value = "/signature/render", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> renderSignature(
            @RequestHeader("Authorization") String authorization,
            @RequestBody(required = false) SignatureRenderRequest request
    ) {
        String token = authorization.replace("Bearer ", "");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(authService.renderSignatureImage(token, request));
    }

    @PostMapping(value = "/signature/certificate-sample", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> renderCertificateSample(
            @RequestHeader("Authorization") String authorization,
            @RequestBody(required = false) CertificateSampleRequest request
    ) {
        String token = authorization.replace("Bearer ", "");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(authService.renderCertificateSample(token, request));
    }

    @PostMapping(value = "/signature/certificate-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> renderKoreanCalligraphyCertificatePdf(
            @RequestHeader("Authorization") String authorization,
            @RequestBody(required = false) CertificateSampleRequest request
    ) {
        String token = authorization.replace("Bearer ", "");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(authService.renderKoreanCalligraphyCertificatePdf(token, request));
    }

    @PostMapping(value = "/signature/certificate-pdf/sample", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> renderKoreanCalligraphyCertificatePdfSample(
            @RequestBody(required = false) CertificateSampleRequest request
    ) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(authService.renderKoreanCalligraphyCertificatePdfSample(request));
    }
}

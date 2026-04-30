package com.example.springboot.controller;

import com.example.springboot.dto.EmailLoginRequest;
import com.example.springboot.dto.EmailCodeSendRequest;
import com.example.springboot.dto.EmailCodeVerifyRequest;
import com.example.springboot.dto.LoginResponse;
import com.example.springboot.dto.SaveSignatureRequest;
import com.example.springboot.dto.VerifyCodeRequest;
import com.example.springboot.dto.VerifyResponse;
import com.example.springboot.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/signature")
    public ResponseEntity<Void> saveSignature(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody SaveSignatureRequest request
    ) {
        String token = authorization.replace("Bearer ", "");
        authService.saveSignature(token, request.signatureDataUrl(), request.recognizedText());
        return ResponseEntity.ok().build();
    }
}

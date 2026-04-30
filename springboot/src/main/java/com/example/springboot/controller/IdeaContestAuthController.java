package com.example.springboot.controller;

import com.example.springboot.dto.EmailLoginRequest;
import com.example.springboot.dto.EmailCodeSendRequest;
import com.example.springboot.dto.EmailCodeVerifyRequest;
import com.example.springboot.dto.LoginResponse;
import com.example.springboot.dto.OAuthAuthorizeResponse;
import com.example.springboot.dto.OAuthExchangeRequest;
import com.example.springboot.service.AuthService;
import com.example.springboot.service.OAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/idea-contest-auth")
public class IdeaContestAuthController {

    private final OAuthService oAuthService;
    private final AuthService authService;

    public IdeaContestAuthController(OAuthService oAuthService, AuthService authService) {
        this.oAuthService = oAuthService;
        this.authService = authService;
    }

    @GetMapping("/oauth/authorize-url/{provider}")
    public ResponseEntity<OAuthAuthorizeResponse> authorizeUrl(
            @PathVariable String provider,
            @RequestParam(value = "redirect", required = false) String redirect
    ) {
        return ResponseEntity.ok(oAuthService.buildAuthorizationUrl(provider, redirect));
    }

    @PostMapping("/oauth/exchange")
    public ResponseEntity<LoginResponse> exchange(@Valid @RequestBody OAuthExchangeRequest request) {
        return ResponseEntity.ok(oAuthService.exchangeCode(request));
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
}

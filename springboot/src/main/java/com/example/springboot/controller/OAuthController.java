package com.example.springboot.controller;

import com.example.springboot.dto.OAuthAuthorizeResponse;
import com.example.springboot.dto.OAuthExchangeRequest;
import com.example.springboot.dto.LoginResponse;
import com.example.springboot.service.OAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/oauth")
public class OAuthController {

    private final OAuthService oAuthService;

    public OAuthController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @GetMapping("/authorize-url/{provider}")
    public ResponseEntity<OAuthAuthorizeResponse> authorizeUrl(
            @PathVariable String provider,
            @RequestParam(value = "redirect", required = false) String redirect
    ) {
        return ResponseEntity.ok(oAuthService.buildAuthorizationUrl(provider, redirect));
    }

    @PostMapping("/exchange")
    public ResponseEntity<LoginResponse> exchange(@Valid @RequestBody OAuthExchangeRequest request) {
        return ResponseEntity.ok(oAuthService.exchangeCode(request));
    }
}

package com.example.springboot.service;

import com.example.springboot.domain.SignupProvider;
import com.example.springboot.dto.LoginResponse;
import com.example.springboot.dto.OAuthAuthorizeResponse;
import com.example.springboot.dto.OAuthExchangeRequest;
import com.example.springboot.security.JwtTokenProvider;
import com.example.springboot.security.TokenType;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class OAuthService {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestClient restClient;

    private final String frontendBaseUrl;
    private final String googleClientId;
    private final String googleClientSecret;
    private final String kakaoClientId;
    private final String kakaoClientSecret;

    public OAuthService(
            AuthService authService,
            JwtTokenProvider jwtTokenProvider,
            @Value("${app.frontend-base-url}") String frontendBaseUrl,
            @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId,
            @Value("${spring.security.oauth2.client.registration.google.client-secret:}") String googleClientSecret,
            @Value("${spring.security.oauth2.client.registration.kakao.client-id:}") String kakaoClientId,
            @Value("${spring.security.oauth2.client.registration.kakao.client-secret:}") String kakaoClientSecret
    ) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.restClient = RestClient.builder().build();
        this.frontendBaseUrl = frontendBaseUrl;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.kakaoClientId = kakaoClientId;
        this.kakaoClientSecret = kakaoClientSecret;
    }

    public OAuthAuthorizeResponse buildAuthorizationUrl(String providerRaw, String redirectPath) {
        SignupProvider provider = parseProvider(providerRaw);
        if (provider == SignupProvider.EMAIL) {
            throw new IllegalArgumentException("EMAIL provider does not use OAuth.");
        }

        String providerId = provider.name().toLowerCase();
        String state = jwtTokenProvider.generateOAuthStateToken(providerId, sanitizeRedirectPath(redirectPath));
        String redirectUri = frontendBaseUrl + "/oauth/callback";

        String url;
        if (provider == SignupProvider.GOOGLE) {
            url = UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                    .queryParam("client_id", googleClientId)
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("response_type", "code")
                    .queryParam("scope", "openid profile email")
                    .queryParam("state", state)
                    .build()
                    .encode()
                    .toUriString();
        } else {
            url = UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
                    .queryParam("client_id", kakaoClientId)
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("response_type", "code")
                    .queryParam("state", state)
                    .build()
                    .encode()
                    .toUriString();
        }

        return new OAuthAuthorizeResponse(providerId, url);
    }

    public OAuthAuthorizeResponse buildAuthorizationUrl(String providerRaw) {
        return buildAuthorizationUrl(providerRaw, null);
    }

    public LoginResponse exchangeCode(OAuthExchangeRequest request) {
        SignupProvider provider = providerFromState(request.state());

        String redirectUri = frontendBaseUrl + "/oauth/callback";
        String accessToken = (provider == SignupProvider.GOOGLE)
                ? exchangeGoogleAccessToken(request.code(), redirectUri)
                : exchangeKakaoAccessToken(request.code(), redirectUri);

        OAuthProfile profile = (provider == SignupProvider.GOOGLE)
                ? fetchGoogleProfile(accessToken)
                : fetchKakaoProfile(accessToken);

        return authService.loginByOAuth(provider, profile);
    }

    private void validateState(String state, SignupProvider provider) {
        TokenType type = jwtTokenProvider.extractTokenType(state);
        if (type != TokenType.OAUTH_STATE) {
            throw new IllegalArgumentException("invalid oauth state token type.");
        }

        String providerInState = jwtTokenProvider.extractSubject(state);
        if (!provider.name().toLowerCase().equals(providerInState)) {
            throw new IllegalArgumentException("oauth state provider mismatch.");
        }
    }

    private SignupProvider providerFromState(String state) {
        TokenType type = jwtTokenProvider.extractTokenType(state);
        if (type != TokenType.OAUTH_STATE) {
            throw new IllegalArgumentException("invalid oauth state token type.");
        }
        String providerInState = jwtTokenProvider.extractSubject(state);
        SignupProvider provider = parseProvider(providerInState);
        if (provider == SignupProvider.EMAIL) {
            throw new IllegalArgumentException("EMAIL provider does not use OAuth exchange.");
        }
        return provider;
    }

    private String exchangeGoogleAccessToken(String code, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", googleClientId);
        form.add("client_secret", googleClientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");

        Map<String, Object> body = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        return tokenFromBody(body);
    }

    private String exchangeKakaoAccessToken(String code, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", kakaoClientId);
        form.add("client_secret", kakaoClientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("code", code);

        Map<String, Object> body = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        return tokenFromBody(body);
    }

    private OAuthProfile fetchGoogleProfile(String accessToken) {
        Map<String, Object> body = restClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        String providerUserId = str(body.get("sub"));
        String name = firstNonBlank(str(body.get("name")), "google-user");
        String email = blankToNull(str(body.get("email")));
        return new OAuthProfile(providerUserId, name, email, null);
    }

    @SuppressWarnings("unchecked")
    private OAuthProfile fetchKakaoProfile(String accessToken) {
        Map<String, Object> body = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        String providerUserId = str(body.get("id"));
        Map<String, Object> kakaoAccount = body.get("kakao_account") instanceof Map
                ? (Map<String, Object>) body.get("kakao_account")
                : Map.of();
        Map<String, Object> profile = kakaoAccount.get("profile") instanceof Map
                ? (Map<String, Object>) kakaoAccount.get("profile")
                : Map.of();

        String name = firstNonBlank(str(kakaoAccount.get("name")), str(profile.get("nickname")), "kakao-user");
        String email = blankToNull(str(kakaoAccount.get("email")));
        String phone = blankToNull(normalizePhone(str(kakaoAccount.get("phone_number"))));
        return new OAuthProfile(providerUserId, name, email, phone);
    }

    private String tokenFromBody(Map<String, Object> body) {
        if (body == null || body.get("access_token") == null) {
            throw new IllegalArgumentException("failed to get access token from provider.");
        }
        return String.valueOf(body.get("access_token"));
    }

    private SignupProvider parseProvider(String providerRaw) {
        try {
            return SignupProvider.valueOf(providerRaw.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("unsupported provider: " + providerRaw);
        }
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private String normalizePhone(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("[^0-9+]", "");
    }

    private String sanitizeRedirectPath(String redirectPath) {
        if (redirectPath == null || redirectPath.isBlank()) {
            return null;
        }
        return redirectPath.startsWith("/") ? redirectPath : null;
    }
}

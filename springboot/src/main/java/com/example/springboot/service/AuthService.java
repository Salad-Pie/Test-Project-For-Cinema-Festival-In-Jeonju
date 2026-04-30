package com.example.springboot.service;

import com.example.springboot.domain.IdentifierCode;
import com.example.springboot.domain.Signature;
import com.example.springboot.domain.SignupProvider;
import com.example.springboot.domain.User;
import com.example.springboot.domain.EmailIdentifierCode;
import com.example.springboot.dto.EmailLoginRequest;
import com.example.springboot.dto.LoginResponse;
import com.example.springboot.dto.VerifyResponse;
import com.example.springboot.repository.EmailIdentifierCodeRepository;
import com.example.springboot.repository.IdentifierCodeRepository;
import com.example.springboot.repository.SignatureRepository;
import com.example.springboot.repository.UserRepository;
import com.example.springboot.security.JwtTokenProvider;
import com.example.springboot.security.TokenType;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final EmailIdentifierCodeRepository emailIdentifierCodeRepository;
    private final IdentifierCodeRepository identifierCodeRepository;
    private final SignatureRepository signatureRepository;
    private final NotificationService notificationService;
    private final GoogleVisionOcrService googleVisionOcrService;
    private final JwtTokenProvider jwtTokenProvider;
    private final String tabletBaseUrl;

    public AuthService(
            UserRepository userRepository,
            EmailIdentifierCodeRepository emailIdentifierCodeRepository,
            IdentifierCodeRepository identifierCodeRepository,
            SignatureRepository signatureRepository,
            NotificationService notificationService,
            GoogleVisionOcrService googleVisionOcrService,
            JwtTokenProvider jwtTokenProvider,
            @Value("${app.tablet-base-url}") String tabletBaseUrl
    ) {
        this.userRepository = userRepository;
        this.emailIdentifierCodeRepository = emailIdentifierCodeRepository;
        this.identifierCodeRepository = identifierCodeRepository;
        this.signatureRepository = signatureRepository;
        this.notificationService = notificationService;
        this.googleVisionOcrService = googleVisionOcrService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tabletBaseUrl = tabletBaseUrl;
    }

    public LoginResponse loginEmail(EmailLoginRequest request) {
        String email = blankToNull(request.email());
        String phone = blankToNull(request.phoneNumber());

        Optional<User> existingUser = findExistingUser(email, phone);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            mergeUser(user, SignupProvider.EMAIL, null, request.name(), email, phone);
            return issueTabletTokenForExisting(user);
        }

        User user = new User();
        user.setProvider(SignupProvider.EMAIL);
        user.setProviderUserId(null);
        user.setName(request.name());
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user = userRepository.save(user);

        return issueIdentifierAndRegisterToken(user, "ko");
    }

    public void sendEmailLoginCode(String emailRaw, String languageRaw) {
        String email = blankToNull(emailRaw);
        if (email == null) {
            throw new IllegalArgumentException("email is required.");
        }
        String language = normalizeLanguage(languageRaw);

        String code = generate6DigitCode();
        EmailIdentifierCode entity = emailIdentifierCodeRepository.findByEmail(email).orElseGet(EmailIdentifierCode::new);
        entity.setEmail(email);
        entity.setCode(code);
        emailIdentifierCodeRepository.save(entity);
        notificationService.sendEmailCode(email, code, language);
    }

    public LoginResponse verifyEmailLoginCode(String emailRaw, String codeRaw) {
        String email = blankToNull(emailRaw);
        String code = blankToNull(codeRaw);

        if (email == null) {
            throw new IllegalArgumentException("email is required.");
        }
        if (code == null) {
            throw new IllegalArgumentException("code is required.");
        }

        EmailIdentifierCode savedCode = emailIdentifierCodeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("identifier code not found for email."));
        if (!savedCode.getCode().equals(code)) {
            throw new IllegalArgumentException("identifier code is invalid.");
        }

        User user = userRepository.findFirstByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setProvider(SignupProvider.EMAIL);
            newUser.setProviderUserId(null);
            newUser.setEmail(email);
            return userRepository.save(newUser);
        });

        TokenType tokenType = user.isVerified() ? TokenType.VERIFIED : TokenType.REGISTER;
        String token = jwtTokenProvider.generateToken(user.getId(), tokenType);
        String tabletUrl = tabletBaseUrl + "?token=" + token;
        return new LoginResponse(user.getId(), token, tabletUrl, "EMAIL:" + email, false, true);
    }

    public LoginResponse loginEmailWithIdentifier(String emailRaw, String codeRaw) {
        String email = blankToNull(emailRaw);
        String code = blankToNull(codeRaw);

        if (email == null) {
            throw new IllegalArgumentException("email is required.");
        }
        if (code == null) {
            throw new IllegalArgumentException("code is required.");
        }

        EmailIdentifierCode savedCode = emailIdentifierCodeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("identifier code not found for email."));
        if (!savedCode.getCode().equals(code)) {
            throw new IllegalArgumentException("identifier code is invalid.");
        }

        User user = userRepository.findFirstByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("user not found for email."));
        return issueTabletTokenForExisting(user);
    }

    public LoginResponse loginByOAuth(SignupProvider provider, OAuthProfile profile, String languageRaw) {
        String email = blankToNull(profile.email());
        String phone = blankToNull(profile.phoneNumber());
        String language = normalizeLanguage(languageRaw);

        Optional<User> existingUser = findExistingUser(email, phone);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            mergeUser(user, provider, profile.providerUserId(), profile.name(), email, phone);
            return issueTabletTokenForExisting(user);
        }

        User user = new User();
        user.setProvider(provider);
        user.setProviderUserId(profile.providerUserId());
        user.setName(profile.name());
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user = userRepository.save(user);

        return issueIdentifierAndRegisterToken(user, language);
    }

    public VerifyResponse verify(String token, String code) {
        validateTokenType(token, TokenType.REGISTER);
        Long userId = jwtTokenProvider.extractUserId(token);

        IdentifierCode latest = identifierCodeRepository.findTopByUserIdOrderByIdDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("identifier code not found."));

        if (!latest.getCode().equals(code)) {
            throw new IllegalArgumentException("identifier code is invalid.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found."));
        user.setVerifiedAt(LocalDateTime.now());

        String verifiedToken = jwtTokenProvider.generateToken(userId, TokenType.VERIFIED);
        return new VerifyResponse(userId, verifiedToken, true);
    }

    public void saveSignature(String verifiedToken, String signatureDataUrl, String recognizedText) {
        validateTokenType(verifiedToken, TokenType.VERIFIED);
        Long userId = jwtTokenProvider.extractUserId(verifiedToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found."));

        if (!user.isVerified()) {
            throw new IllegalArgumentException("only verified users can save signature.");
        }

        Signature signature = signatureRepository.findByUserId(userId).orElseGet(Signature::new);
        signature.setUser(user);
        signature.setSignatureDataUrl(signatureDataUrl);
        String ocrText = googleVisionOcrService.extractTextFromDataUrl(signatureDataUrl);
        String finalText = blankToNull(ocrText);
        if (finalText == null) {
            finalText = blankToNull(recognizedText);
        }
        signature.setRecognizedText(finalText);
        signatureRepository.save(signature);
    }

    private LoginResponse issueIdentifierAndRegisterToken(User user, String language) {
        String sentTo = "NONE";
        boolean codeSent = false;

        if (user.getEmail() != null) {
            String code = generate6DigitCode();
            saveIdentifierCode(user, code);
            notificationService.sendEmailCode(user.getEmail(), code, language);
            sentTo = "EMAIL:" + user.getEmail();
            codeSent = true;
        } else if (user.getPhoneNumber() != null) {
            String code = generate6DigitCode();
            saveIdentifierCode(user, code);
            notificationService.sendSmsCode(user.getPhoneNumber(), code);
            sentTo = "PHONE:" + user.getPhoneNumber();
            codeSent = true;
        }

        String registerToken = jwtTokenProvider.generateToken(user.getId(), TokenType.REGISTER);
        String tabletUrl = tabletBaseUrl + "?token=" + registerToken;
        return new LoginResponse(user.getId(), registerToken, tabletUrl, sentTo, false, codeSent);
    }

    private LoginResponse issueTabletTokenForExisting(User user) {
        TokenType tokenType = user.isVerified() ? TokenType.VERIFIED : TokenType.REGISTER;
        String token = jwtTokenProvider.generateToken(user.getId(), tokenType);
        String tabletUrl = tabletBaseUrl + "?token=" + token;
        return new LoginResponse(user.getId(), token, tabletUrl, "NONE", true, false);
    }

    private Optional<User> findExistingUser(String email, String phone) {
        if (email != null) {
            Optional<User> byEmail = userRepository.findFirstByEmail(email);
            if (byEmail.isPresent()) {
                return byEmail;
            }
        }

        if (phone != null) {
            return userRepository.findFirstByPhoneNumber(phone);
        }

        return Optional.empty();
    }

    private void mergeUser(User user, SignupProvider provider, String providerUserId, String name, String email, String phone) {
        if (provider != null) {
            user.setProvider(provider);
        }
        if (providerUserId != null && !providerUserId.isBlank()) {
            user.setProviderUserId(providerUserId);
        }
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }
        if (phone != null && !phone.isBlank()) {
            user.setPhoneNumber(phone);
        }
    }

    private void validateTokenType(String token, TokenType expected) {
        TokenType tokenType = jwtTokenProvider.extractTokenType(token);
        if (tokenType != expected) {
            throw new IllegalArgumentException("token type is not valid for this request.");
        }
    }

    private String generate6DigitCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private String normalizeLanguage(String languageRaw) {
        String language = blankToNull(languageRaw);
        if (language == null) {
            return "ko";
        }
        return switch (language.toLowerCase()) {
            case "en", "zh", "ja" -> language.toLowerCase();
            default -> "ko";
        };
    }

    private void saveIdentifierCode(User user, String code) {
        IdentifierCode identifierCode = new IdentifierCode();
        identifierCode.setUser(user);
        identifierCode.setCode(code);
        identifierCodeRepository.save(identifierCode);
    }
}

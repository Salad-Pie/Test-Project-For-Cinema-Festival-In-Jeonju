package com.example.springboot.service;

import com.example.springboot.domain.IdentifierCode;
import com.example.springboot.domain.OcrProvider;
import com.example.springboot.domain.OcrStatus;
import com.example.springboot.domain.Signature;
import com.example.springboot.domain.SignatureLanguage;
import com.example.springboot.domain.SignupProvider;
import com.example.springboot.domain.User;
import com.example.springboot.domain.EmailIdentifierCode;
import com.example.springboot.dto.EmailLoginRequest;
import com.example.springboot.dto.CertificateSampleRequest;
import com.example.springboot.dto.LoginResponse;
import com.example.springboot.dto.SignatureRenderRequest;
import com.example.springboot.dto.SignatureResponse;
import com.example.springboot.dto.VerifyResponse;
import com.example.springboot.exception.BusinessException;
import com.example.springboot.exception.ErrorCode;
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
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final EmailIdentifierCodeRepository emailIdentifierCodeRepository;
    private final IdentifierCodeRepository identifierCodeRepository;
    private final SignatureRepository signatureRepository;
    private final NotificationService notificationService;
    private final GoogleVisionOcrService googleVisionOcrService;
    private final S3UploadService s3UploadService;
    private final EnglishKoreanNameService englishKoreanNameService;
    private final GoogleTranslationService googleTranslationService;
    private final SignatureImageService signatureImageService;
    private final JwtTokenProvider jwtTokenProvider;
    private final String tabletBaseUrl;

    public AuthService(
            UserRepository userRepository,
            EmailIdentifierCodeRepository emailIdentifierCodeRepository,
            IdentifierCodeRepository identifierCodeRepository,
            SignatureRepository signatureRepository,
            NotificationService notificationService,
            GoogleVisionOcrService googleVisionOcrService,
            S3UploadService s3UploadService,
            EnglishKoreanNameService englishKoreanNameService,
            GoogleTranslationService googleTranslationService,
            SignatureImageService signatureImageService,
            JwtTokenProvider jwtTokenProvider,
            @Value("${app.tablet-base-url}") String tabletBaseUrl
    ) {
        this.userRepository = userRepository;
        this.emailIdentifierCodeRepository = emailIdentifierCodeRepository;
        this.identifierCodeRepository = identifierCodeRepository;
        this.signatureRepository = signatureRepository;
        this.notificationService = notificationService;
        this.googleVisionOcrService = googleVisionOcrService;
        this.s3UploadService = s3UploadService;
        this.englishKoreanNameService = englishKoreanNameService;
        this.googleTranslationService = googleTranslationService;
        this.signatureImageService = signatureImageService;
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

        saveIdentifierCode(user, code);

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
        if (blankToNull(token) == null) {
            return verifyByIdentifierCode(code);
        }

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

    private VerifyResponse verifyByIdentifierCode(String codeRaw) {
        String code = blankToNull(codeRaw);
        if (code == null) {
            throw new IllegalArgumentException("identifier code is required.");
        }

        IdentifierCode identifierCode = identifierCodeRepository.findTopByCodeOrderByIdDesc(code)
                .orElseThrow(() -> new IllegalArgumentException("identifier code is invalid."));
        User user = identifierCode.getUser();
        user.setVerifiedAt(LocalDateTime.now());

        String verifiedToken = jwtTokenProvider.generateToken(user.getId(), TokenType.VERIFIED);
        return new VerifyResponse(user.getId(), verifiedToken, true);
    }

    public SignatureResponse saveSignature(String verifiedToken, MultipartFile signatureImage) {
        validateTokenType(verifiedToken, TokenType.VERIFIED);
        Long userId = jwtTokenProvider.extractUserId(verifiedToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found."));

        if (!user.isVerified()) {
            throw new IllegalArgumentException("only verified users can save signature.");
        }

        S3UploadService.UploadedImage uploadedImage = s3UploadService.uploadSignatureOriginal(userId, signatureImage);
        GoogleVisionOcrService.OcrResult ocrResult = googleVisionOcrService.extractSignatureText(signatureImage);
        String recognizedText = normalizeOcrText(ocrResult.text());
        SignatureLanguage language = detectKoOrEn(recognizedText);
        if (recognizedText == null || language == null) {
            throw new BusinessException(ErrorCode.OCR_RECOGNITION_FAILED);
        }
        String koreanText = resolveKoreanText(recognizedText, language);
        String koreanMeaningText = resolveKoreanMeaningText(recognizedText, language);

        Signature signature = signatureRepository.findByUserId(userId).orElseGet(Signature::new);
        signature.setUser(user);
        signature.setOriginalS3Key(uploadedImage.s3Key());
        signature.setOriginalFileSize(uploadedImage.fileSize());
        signature.setOriginalContentType("image/png");
        signature.setRecognizedText(recognizedText);
        signature.setKoreanText(koreanText);
        signature.setKoreanMeaningText(koreanMeaningText);
        signature.setDetectedLanguage(language);
        signature.setOcrConfidence(ocrResult.confidence());
        signature.setOcrProvider(OcrProvider.GOOGLE_VISION);
        signature.setOcrStatus(OcrStatus.SUCCESS);
        signature.setOcrErrorMessage(null);
        signature.setOcrProcessedAt(LocalDateTime.now());
        Signature saved = signatureRepository.save(signature);
        return new SignatureResponse(
                saved.getId(),
                saved.getRecognizedText(),
                saved.getKoreanText(),
                saved.getKoreanMeaningText(),
                saved.getDetectedLanguage(),
                saved.getOcrStatus(),
                saved.getOcrConfidence()
        );
    }

    public byte[] renderSignatureImage(String verifiedToken, SignatureRenderRequest request) {
        validateTokenType(verifiedToken, TokenType.VERIFIED);
        Long userId = jwtTokenProvider.extractUserId(verifiedToken);
        Signature signature = signatureRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("signature not found."));
        String text = request == null ? null : request.text();
        if (text == null || text.isBlank()) {
            text = signature.getKoreanText() == null ? signature.getRecognizedText() : signature.getKoreanText();
        }
        return signatureImageService.renderSignature(
                text,
                request == null ? null : request.fontFamily(),
                request == null ? null : request.fontSize(),
                request == null ? null : request.width(),
                request == null ? null : request.height()
        );
    }

    public byte[] renderCertificateSample(String verifiedToken, CertificateSampleRequest request) {
        validateTokenType(verifiedToken, TokenType.VERIFIED);
        Long userId = jwtTokenProvider.extractUserId(verifiedToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found."));
        Signature signature = signatureRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("signature not found."));

        String defaultName = user.getName();
        if (defaultName == null || defaultName.isBlank()) {
            defaultName = user.getNickname();
        }
        String defaultSignature = signature.getKoreanText() == null ? signature.getRecognizedText() : signature.getKoreanText();
        return signatureImageService.renderCertificateSample(request, defaultName, defaultSignature);
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

    private String normalizeOcrText(String rawText) {
        String value = blankToNull(rawText);
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\s+", " ").trim();
    }

    private SignatureLanguage detectKoOrEn(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        long koreanCount = text.chars()
                .filter(ch -> (ch >= 0xAC00 && ch <= 0xD7A3) || (ch >= 0x3131 && ch <= 0x318E))
                .count();
        long englishCount = text.chars()
                .filter(ch -> (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z'))
                .count();

        if (koreanCount == 0 && englishCount == 0) {
            return null;
        }
        if (koreanCount >= englishCount) {
            return SignatureLanguage.KO;
        }
        return SignatureLanguage.EN;
    }

    private String resolveKoreanText(String recognizedText, SignatureLanguage language) {
        if (language == SignatureLanguage.KO) {
            return recognizedText;
        }
        return englishKoreanNameService.toKoreanPronunciation(recognizedText);
    }

    private String resolveKoreanMeaningText(String recognizedText, SignatureLanguage language) {
        if (language == SignatureLanguage.KO) {
            return recognizedText;
        }
        return googleTranslationService.translateEnglishToKorean(recognizedText);
    }

    private void saveIdentifierCode(User user, String code) {
        IdentifierCode identifierCode = new IdentifierCode();
        identifierCode.setUser(user);
        identifierCode.setCode(code);
        identifierCodeRepository.save(identifierCode);
    }
}

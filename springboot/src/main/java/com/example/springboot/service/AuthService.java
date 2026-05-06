package com.example.springboot.service;

import com.example.springboot.domain.IdentifierCode;
import com.example.springboot.domain.NameConversionSource;
import com.example.springboot.domain.NameLanguage;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int CODE_GENERATION_MAX_ATTEMPTS = 20;

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
    private final CertificatePdfService certificatePdfService;
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
            CertificatePdfService certificatePdfService,
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
        this.certificatePdfService = certificatePdfService;
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

        String code = issueEmailIdentifierCode(email);
        log.info("Email login identifier code issued. email={} codeSuffix={}", maskEmail(email), codeSuffix(code));
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

    public SignatureResponse saveSignature(String verifiedToken, MultipartFile signatureImage, String nameLanguageRaw, String koreanNameOverrideRaw) {
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
        NameLanguage nameLanguage = normalizeNameLanguage(nameLanguageRaw, language);
        String koreanNameOverride = blankToNull(koreanNameOverrideRaw);
        EnglishKoreanNameService.ConversionResult nameConversion = resolveNameConversion(recognizedText, language, nameLanguage, koreanNameOverride);
        String koreanText = resolveKoreanText(recognizedText, language, nameConversion);
        String koreanMeaningText = resolveKoreanMeaningText(recognizedText, language);
        String englishName = resolveEnglishName(recognizedText, language);
        String koreanName = resolveKoreanName(recognizedText, koreanText, language);
        NameConversionSource nameConversionSource = resolveNameConversionSource(language, nameConversion, koreanNameOverride);

        Signature signature = signatureRepository.findByUserId(userId).orElseGet(Signature::new);
        signature.setUser(user);
        signature.setOriginalS3Key(uploadedImage.s3Key());
        signature.setOriginalFileSize(uploadedImage.fileSize());
        signature.setOriginalContentType("image/png");
        signature.setRecognizedText(recognizedText);
        signature.setOriginalName(recognizedText);
        signature.setNameLanguage(nameLanguage);
        signature.setKoreanText(koreanText);
        signature.setKoreanMeaningText(koreanMeaningText);
        signature.setEnglishName(englishName);
        signature.setKoreanName(koreanName);
        signature.setNameConversionSource(nameConversionSource);
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
                saved.getOriginalName(),
                saved.getNameLanguage(),
                saved.getKoreanText(),
                saved.getKoreanMeaningText(),
                saved.getEnglishName(),
                saved.getKoreanName(),
                saved.getNameConversionSource(),
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

    public byte[] renderKoreanCalligraphyCertificatePdf(String verifiedToken, CertificateSampleRequest request) {
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
        String defaultEnglishName = blankToDefault(signature.getEnglishName(), defaultName);
        String defaultKoreanName = blankToDefault(signature.getKoreanName(), signature.getKoreanText());
        defaultKoreanName = blankToDefault(defaultKoreanName, signature.getRecognizedText());
        String englishName = blankToDefault(request == null ? null : request.englishName(), defaultEnglishName);
        String koreanName = blankToDefault(request == null ? null : request.koreanName(), defaultKoreanName);
        byte[] originalSignature = s3UploadService.downloadObjectBytes(signature.getOriginalS3Key());
        return certificatePdfService.renderKoreanCalligraphyCertificate(englishName, koreanName, originalSignature);
    }

    public byte[] renderKoreanCalligraphyCertificatePdfSample(CertificateSampleRequest request) {
        String englishName = blankToDefault(request == null ? null : request.englishName(), "ALEXANDER MICHAEL JOHNSON");
        String koreanName = blankToDefault(request == null ? null : request.koreanName(), "김도윤");
        return certificatePdfService.renderKoreanCalligraphyCertificate(englishName, koreanName, null);
    }

    public byte[] renderStrongCalligraphyTextSample(SignatureRenderRequest request) {
        String text = blankToDefault(request == null ? null : request.text(), "이창섭");
        return signatureImageService.renderStrongCalligraphyText(
                text,
                request == null ? null : request.width(),
                request == null ? null : request.height()
        );
    }

    private LoginResponse issueIdentifierAndRegisterToken(User user, String language) {
        String sentTo = "NONE";
        boolean codeSent = false;

        if (user.getEmail() != null) {
            String code = issueEmailIdentifierCode(user.getEmail());
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

    private String issueEmailIdentifierCode(String email) {
        String normalizedEmail = blankToNull(email);
        if (normalizedEmail == null) {
            throw new IllegalArgumentException("email is required.");
        }
        String code = generateUniqueEmailIdentifierCode(normalizedEmail);
        EmailIdentifierCode entity = emailIdentifierCodeRepository.findByEmail(normalizedEmail).orElseGet(EmailIdentifierCode::new);
        entity.setEmail(normalizedEmail);
        entity.setCode(code);
        emailIdentifierCodeRepository.save(entity);
        return code;
    }

    private String generateUniqueEmailIdentifierCode(String email) {
        for (int attempt = 1; attempt <= CODE_GENERATION_MAX_ATTEMPTS; attempt++) {
            String code = generate6DigitCode();
            if (!emailIdentifierCodeRepository.existsByCodeAndEmailNot(code, email)) {
                return code;
            }
            log.info("Email identifier code collision detected. attempt={} codeSuffix={}", attempt, codeSuffix(code));
        }
        throw new IllegalStateException("failed to generate unique email identifier code.");
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    private String codeSuffix(String code) {
        if (code == null || code.length() < 2) {
            return "**";
        }
        return "**" + code.substring(code.length() - 2);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private String blankToDefault(String value, String defaultValue) {
        String normalized = blankToNull(value);
        return normalized == null ? defaultValue : normalized;
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

    private String resolveKoreanText(String recognizedText, SignatureLanguage language, EnglishKoreanNameService.ConversionResult nameConversion) {
        if (language == SignatureLanguage.KO) {
            return recognizedText;
        }
        return nameConversion.koreanName();
    }

    private String resolveKoreanMeaningText(String recognizedText, SignatureLanguage language) {
        if (language == SignatureLanguage.KO) {
            return recognizedText;
        }
        return googleTranslationService.translateEnglishToKorean(recognizedText);
    }

    private String resolveEnglishName(String recognizedText, SignatureLanguage language) {
        if (language == SignatureLanguage.EN) {
            return recognizedText;
        }
        return null;
    }

    private String resolveKoreanName(String recognizedText, String koreanText, SignatureLanguage language) {
        if (language == SignatureLanguage.KO) {
            return recognizedText;
        }
        return koreanText;
    }

    private EnglishKoreanNameService.ConversionResult resolveNameConversion(
            String recognizedText,
            SignatureLanguage language,
            NameLanguage nameLanguage,
            String koreanNameOverride
    ) {
        if (koreanNameOverride != null) {
            return new EnglishKoreanNameService.ConversionResult(koreanNameOverride, NameConversionSource.MANUAL);
        }
        if (language == SignatureLanguage.KO) {
            return new EnglishKoreanNameService.ConversionResult(recognizedText, NameConversionSource.MANUAL);
        }
        return englishKoreanNameService.convertByOfficialLoanwordPolicy(recognizedText, nameLanguage);
    }

    private NameConversionSource resolveNameConversionSource(
            SignatureLanguage language,
            EnglishKoreanNameService.ConversionResult nameConversion,
            String koreanNameOverride
    ) {
        if (koreanNameOverride != null) {
            return NameConversionSource.MANUAL;
        }
        if (language == SignatureLanguage.KO) {
            return NameConversionSource.MANUAL;
        }
        return nameConversion.source();
    }

    private NameLanguage normalizeNameLanguage(String nameLanguageRaw, SignatureLanguage detectedLanguage) {
        String value = blankToNull(nameLanguageRaw);
        if (value == null) {
            return detectedLanguage == SignatureLanguage.KO ? NameLanguage.OTHER : NameLanguage.EN;
        }
        try {
            return NameLanguage.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return detectedLanguage == SignatureLanguage.KO ? NameLanguage.OTHER : NameLanguage.EN;
        }
    }

    private void saveIdentifierCode(User user, String code) {
        IdentifierCode identifierCode = new IdentifierCode();
        identifierCode.setUser(user);
        identifierCode.setCode(code);
        identifierCodeRepository.save(identifierCode);
    }
}

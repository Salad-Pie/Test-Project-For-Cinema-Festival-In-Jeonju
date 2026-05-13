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
import com.example.springboot.dto.EmailLoginRequest;
import com.example.springboot.dto.CertificateSampleRequest;
import com.example.springboot.dto.IdentifierCodeReissueRequest;
import com.example.springboot.dto.IdentifierCodeReissueResponse;
import com.example.springboot.dto.LoginResponse;
import com.example.springboot.dto.SignaturePreviewResponse;
import com.example.springboot.dto.SignatureRenderRequest;
import com.example.springboot.dto.SignatureResponse;
import com.example.springboot.dto.VerifyResponse;
import com.example.springboot.exception.BusinessException;
import com.example.springboot.exception.ErrorCode;
import com.example.springboot.repository.IdentifierCodeRepository;
import com.example.springboot.repository.SignatureRepository;
import com.example.springboot.repository.UserRepository;
import com.example.springboot.security.JwtTokenProvider;
import com.example.springboot.security.TokenType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
    private static final int IDENTIFIER_REISSUE_MAX_ATTEMPTS = 5;
    private static final Duration IDENTIFIER_REISSUE_WINDOW = Duration.ofMinutes(10);
    private static final Duration SIGNATURE_PREVIEW_EXPIRATION = Duration.ofMinutes(10);
    private static final String IDENTIFIER_REISSUE_RESPONSE_MESSAGE = "입력하신 정보가 등록되어 있다면 새 식별자 코드를 전송했습니다.";

    private final UserRepository userRepository;
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
    private final PointRewardService pointRewardService;
    private final String tabletBaseUrl;
    private final Map<String, ReissueRateLimit> identifierReissueRateLimits = new ConcurrentHashMap<>();
    private final Map<String, SignaturePreviewSession> signaturePreviewSessions = new ConcurrentHashMap<>();

    public AuthService(
            UserRepository userRepository,
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
            PointRewardService pointRewardService,
            @Value("${app.tablet-base-url}") String tabletBaseUrl
    ) {
        this.userRepository = userRepository;
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
        this.pointRewardService = pointRewardService;
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

        pointRewardService.earnActivityPoints(user.getId(), "신규 회원가입 축하 포인트 (EMAIL)");
        return issueIdentifierAndRegisterToken(user, "ko");
    }

    public void sendEmailLoginCode(String emailRaw, String languageRaw) {
        String email = blankToNull(emailRaw);
        if (email == null) {
            throw new IllegalArgumentException("email is required.");
        }
        String language = normalizeLanguage(languageRaw);

        User user = userRepository.findFirstByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setProvider(SignupProvider.EMAIL);
            newUser.setProviderUserId(null);
            newUser.setEmail(email);
            return userRepository.save(newUser);
        });
        String code = issueIdentifierCode(user);
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

        IdentifierCode savedCode = identifierCodeRepository.findTopByUserEmailOrderByIdDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("identifier code not found for email."));
        if (!savedCode.getCode().equals(code)) {
            throw new IllegalArgumentException("identifier code is invalid.");
        }

        User user = savedCode.getUser();

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

        IdentifierCode savedCode = identifierCodeRepository.findTopByUserEmailOrderByIdDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("identifier code not found for email."));
        if (!savedCode.getCode().equals(code)) {
            throw new IllegalArgumentException("identifier code is invalid.");
        }

        User user = savedCode.getUser();
        return issueTabletTokenForExisting(user);
    }

    public IdentifierCodeReissueResponse reissueIdentifierCode(IdentifierCodeReissueRequest request, String token, String remoteAddr) {
        String language = normalizeLanguage(request.language());

        validateLoggedInToken(token);
        Long userId = jwtTokenProvider.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found."));

        String email = blankToNull(user.getEmail());
        String phone = blankToNull(user.getPhoneNumber());
        String rateLimitKey = buildReissueRateLimitKey(userId, remoteAddr);
        if (isIdentifierReissueRateLimited(rateLimitKey)) {
            log.info("Identifier code reissue rate limited. key={}", maskRateLimitKey(rateLimitKey));
            return new IdentifierCodeReissueResponse(IDENTIFIER_REISSUE_RESPONSE_MESSAGE);
        }

        if (email != null) {
            String code = issueIdentifierCode(user);
            notificationService.sendEmailCode(email, code, language);
            log.info("Identifier code reissued by email. userId={} email={} codeSuffix={}", user.getId(), maskEmail(email), codeSuffix(code));
            return new IdentifierCodeReissueResponse(IDENTIFIER_REISSUE_RESPONSE_MESSAGE);
        }

        if (phone != null) {
            String code = issueIdentifierCode(user);
            notificationService.sendSmsCode(phone, code);
            log.info("Identifier code reissued by phone. userId={} phone={} codeSuffix={}", user.getId(), maskPhone(phone), codeSuffix(code));
            return new IdentifierCodeReissueResponse(IDENTIFIER_REISSUE_RESPONSE_MESSAGE);
        }

        log.info("Identifier code reissue skipped because user has no delivery channel. userId={}", user.getId());
        return new IdentifierCodeReissueResponse(IDENTIFIER_REISSUE_RESPONSE_MESSAGE);
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

        pointRewardService.earnActivityPoints(user.getId(), "신규 회원가입 축하 포인트 (OAUTH)");
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
        User user = getVerifiedUser(userId);
        SignaturePreviewSession session = buildSignaturePreviewSession(userId, signatureImage, nameLanguageRaw, koreanNameOverrideRaw);
        return persistSignaturePreviewSession(user, session);
    }

    public SignaturePreviewResponse previewSignature(String verifiedToken, MultipartFile signatureImage, String nameLanguageRaw, String koreanNameOverrideRaw) {
        validateTokenType(verifiedToken, TokenType.VERIFIED);
        Long userId = jwtTokenProvider.extractUserId(verifiedToken);
        getVerifiedUser(userId);

        cleanupExpiredSignaturePreviewSessions();
        SignaturePreviewSession session = buildSignaturePreviewSession(userId, signatureImage, nameLanguageRaw, koreanNameOverrideRaw);
        String previewToken = UUID.randomUUID().toString();
        signaturePreviewSessions.put(previewToken, session);
        log.info("Signature preview created. userId={} previewToken={}", userId, previewToken);
        return toSignaturePreviewResponse(previewToken, session);
    }

    public SignatureResponse confirmSignaturePreview(String verifiedToken, String previewTokenRaw) {
        validateTokenType(verifiedToken, TokenType.VERIFIED);
        Long userId = jwtTokenProvider.extractUserId(verifiedToken);
        User user = getVerifiedUser(userId);
        cleanupExpiredSignaturePreviewSessions();

        String previewToken = blankToNull(previewTokenRaw);
        if (previewToken == null) {
            throw new IllegalArgumentException("preview token is required.");
        }

        SignaturePreviewSession session = signaturePreviewSessions.get(previewToken);
        if (session == null || session.expiresAt().isBefore(LocalDateTime.now())) {
            signaturePreviewSessions.remove(previewToken);
            throw new IllegalArgumentException("signature preview not found.");
        }
        if (!session.userId().equals(userId)) {
            throw new SecurityException("signature preview user does not match.");
        }

        signaturePreviewSessions.remove(previewToken);
        log.info("Signature preview confirmed. userId={} previewToken={}", userId, previewToken);
        return persistSignaturePreviewSession(user, session);
    }


    private void deletePreviousSignatureFiles(Signature signature) {
        if (signature.getOriginalS3Key() != null && !signature.getOriginalS3Key().isBlank()) {
            s3UploadService.deleteObject(signature.getOriginalS3Key());
        }
        if (signature.getPreprocessedS3Key() != null && !signature.getPreprocessedS3Key().isBlank()) {
            s3UploadService.deleteObject(signature.getPreprocessedS3Key());
            signature.setPreprocessedS3Key(null);
        }
        if (signature.getOcrRawResponseS3Key() != null && !signature.getOcrRawResponseS3Key().isBlank()) {
            s3UploadService.deleteObject(signature.getOcrRawResponseS3Key());
            signature.setOcrRawResponseS3Key(null);
        }
    }

    private User getVerifiedUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found."));
        if (!user.isVerified()) {
            throw new IllegalArgumentException("only verified users can save signature.");
        }
        return user;
    }

    private SignaturePreviewSession buildSignaturePreviewSession(Long userId, MultipartFile signatureImage, String nameLanguageRaw, String koreanNameOverrideRaw) {
        try {
            byte[] signatureImageBytes = signatureImage.getBytes();
            GoogleVisionOcrService.OcrResult ocrResult = googleVisionOcrService.extractSignatureText(signatureImage);
            String recognizedText = normalizeOcrText(ocrResult.text());
            SignatureLanguage language = detectLanguage(recognizedText);
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
            log.info("Signature preview OCR completed. userId={} detectedLanguage={} confidence={}", userId, language, ocrResult.confidence());
            return new SignaturePreviewSession(
                    userId,
                    signatureImageBytes,
                    "image/png",
                    (long) signatureImageBytes.length,
                    recognizedText,
                    nameLanguage,
                    koreanText,
                    koreanMeaningText,
                    englishName,
                    koreanName,
                    nameConversionSource,
                    language,
                    ocrResult.confidence(),
                    LocalDateTime.now().plus(SIGNATURE_PREVIEW_EXPIRATION)
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("failed to build signature preview session.", ex);
        }
    }

    private SignaturePreviewResponse toSignaturePreviewResponse(String previewToken, SignaturePreviewSession session) {
        return new SignaturePreviewResponse(
                previewToken,
                session.recognizedText(),
                session.recognizedText(),
                session.nameLanguage(),
                session.koreanText(),
                session.koreanMeaningText(),
                session.englishName(),
                session.koreanName(),
                session.nameConversionSource(),
                session.detectedLanguage(),
                OcrStatus.SUCCESS,
                session.ocrConfidence()
        );
    }

    private SignatureResponse persistSignaturePreviewSession(User user, SignaturePreviewSession session) {
        S3UploadService.UploadedImage uploadedImage =
                s3UploadService.uploadSignatureOriginal(user.getId(), session.signatureImageBytes(), session.contentType());

        Signature signature = signatureRepository.findByUserId(user.getId()).orElseGet(Signature::new);
        if (signature.getId() != null) {
            log.info("Existing signature row will be updated. userId={} signatureId={}", user.getId(), signature.getId());
            deletePreviousSignatureFiles(signature);
        }

        signature.setUser(user);
        signature.setOriginalS3Key(uploadedImage.s3Key());
        signature.setOriginalFileSize(uploadedImage.fileSize());
        signature.setOriginalContentType(session.contentType());
        signature.setRecognizedText(session.recognizedText());
        signature.setOriginalName(session.recognizedText());
        signature.setNameLanguage(session.nameLanguage());
        signature.setKoreanText(session.koreanText());
        signature.setKoreanMeaningText(session.koreanMeaningText());
        signature.setEnglishName(session.englishName());
        signature.setKoreanName(session.koreanName());
        signature.setNameConversionSource(session.nameConversionSource());
        signature.setDetectedLanguage(session.detectedLanguage());
        signature.setOcrConfidence(session.ocrConfidence());
        signature.setOcrProvider(OcrProvider.GOOGLE_VISION);
        signature.setOcrStatus(OcrStatus.SUCCESS);
        signature.setOcrErrorMessage(null);
        signature.setOcrProcessedAt(LocalDateTime.now());
        Signature saved = signatureRepository.save(signature);
        pointRewardService.earnActivityPoints(user.getId(), "디지털 서명 참여 리워드");
        log.info("Signature saved from session. userId={} signatureId={}", user.getId(), saved.getId());
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

    private void cleanupExpiredSignaturePreviewSessions() {
        LocalDateTime now = LocalDateTime.now();
        signaturePreviewSessions.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
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
        String koreanName = blankToDefault(request == null ? null : request.koreanName(), "이창섭");
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
            String code = issueIdentifierCode(user);
            notificationService.sendEmailCode(user.getEmail(), code, language);
            sentTo = "EMAIL:" + user.getEmail();
            codeSent = true;
        } else if (user.getPhoneNumber() != null) {
            String code = issueIdentifierCode(user);
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

    private void validateLoggedInToken(String token) {
        TokenType tokenType = jwtTokenProvider.extractTokenType(token);
        if (tokenType != TokenType.REGISTER && tokenType != TokenType.VERIFIED) {
            throw new IllegalArgumentException("token type is not valid for this request.");
        }
    }

    private String generate6DigitCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    private String issueIdentifierCode(User user) {
        String code = generateUniqueIdentifierCode();
        saveIdentifierCode(user, code);
        return code;
    }

    private String generateUniqueIdentifierCode() {
        for (int attempt = 1; attempt <= CODE_GENERATION_MAX_ATTEMPTS; attempt++) {
            String code = generate6DigitCode();
            if (!identifierCodeRepository.existsByCode(code)) {
                return code;
            }
            log.info("Identifier code collision detected. attempt={} codeSuffix={}", attempt, codeSuffix(code));
        }
        throw new IllegalStateException("failed to generate unique identifier code.");
    }

    private boolean isIdentifierReissueRateLimited(String key) {
        LocalDateTime now = LocalDateTime.now();
        ReissueRateLimit current = identifierReissueRateLimits.get(key);

        if (current == null || current.windowStartedAt().plus(IDENTIFIER_REISSUE_WINDOW).isBefore(now)) {
            identifierReissueRateLimits.put(key, new ReissueRateLimit(now, 1));
            return false;
        }

        int nextAttempts = current.attempts() + 1;
        identifierReissueRateLimits.put(key, new ReissueRateLimit(current.windowStartedAt(), nextAttempts));
        return nextAttempts > IDENTIFIER_REISSUE_MAX_ATTEMPTS;
    }

    private String buildReissueRateLimitKey(Long userId, String remoteAddr) {
        String target = "user:" + userId;
        String ip = blankToNull(remoteAddr) == null ? "unknown" : remoteAddr;
        return ip + "|" + target;
    }

    private String maskRateLimitKey(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        int separator = key.indexOf('|');
        if (separator < 0) {
            return "***";
        }
        return key.substring(0, separator) + "|***";
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

    private String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "";
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() <= 4) {
            return "****";
        }
        return "***-****-" + digits.substring(digits.length() - 4);
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

    private SignatureLanguage detectLanguage(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        long koreanCount = text.chars()
                .filter(ch -> (ch >= 0xAC00 && ch <= 0xD7A3) || (ch >= 0x3131 && ch <= 0x318E))
                .count();
        long hanziCount = text.chars()
                .filter(ch -> (ch >= 0x4E00 && ch <= 0x9FFF))
                .count();
        long englishCount = text.chars()
                .filter(ch -> (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z'))
                .count();

        if (koreanCount == 0 && hanziCount == 0 && englishCount == 0) {
            return null;
        }

        if (koreanCount >= hanziCount && koreanCount >= englishCount) {
            return SignatureLanguage.KO;
        }
        if (hanziCount >= englishCount) {
            return SignatureLanguage.ZH;
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
            if (detectedLanguage == SignatureLanguage.KO) return NameLanguage.OTHER;
            if (detectedLanguage == SignatureLanguage.ZH) return NameLanguage.ZH;
            return NameLanguage.EN;
        }
        try {
            return NameLanguage.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            if (detectedLanguage == SignatureLanguage.KO) return NameLanguage.OTHER;
            if (detectedLanguage == SignatureLanguage.ZH) return NameLanguage.ZH;
            return NameLanguage.EN;
        }
    }

    private void saveIdentifierCode(User user, String code) {
        IdentifierCode identifierCode = new IdentifierCode();
        identifierCode.setUser(user);
        identifierCode.setCode(code);
        identifierCodeRepository.save(identifierCode);
    }

    private record ReissueRateLimit(
            LocalDateTime windowStartedAt,
            int attempts
    ) {
    }

    private record SignaturePreviewSession(
            Long userId,
            byte[] signatureImageBytes,
            String contentType,
            Long fileSize,
            String recognizedText,
            NameLanguage nameLanguage,
            String koreanText,
            String koreanMeaningText,
            String englishName,
            String koreanName,
            NameConversionSource nameConversionSource,
            SignatureLanguage detectedLanguage,
            Double ocrConfidence,
            LocalDateTime expiresAt
    ) {
    }
}

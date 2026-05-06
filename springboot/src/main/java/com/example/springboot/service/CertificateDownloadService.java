package com.example.springboot.service;

import com.example.springboot.domain.IdentifierCode;
import com.example.springboot.domain.Signature;
import com.example.springboot.exception.BusinessException;
import com.example.springboot.exception.ErrorCode;
import com.example.springboot.repository.IdentifierCodeRepository;
import com.example.springboot.repository.SignatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CertificateDownloadService {

    private static final Logger log = LoggerFactory.getLogger(CertificateDownloadService.class);
    private static final int SIGNATURE_IMAGE_WIDTH = 1400;
    private static final int SIGNATURE_IMAGE_HEIGHT = 520;

    private final IdentifierCodeRepository identifierCodeRepository;
    private final SignatureRepository signatureRepository;
    private final SignatureImageService signatureImageService;
    private final CertificatePdfService certificatePdfService;

    public CertificateDownloadService(
            IdentifierCodeRepository identifierCodeRepository,
            SignatureRepository signatureRepository,
            SignatureImageService signatureImageService,
            CertificatePdfService certificatePdfService
    ) {
        this.identifierCodeRepository = identifierCodeRepository;
        this.signatureRepository = signatureRepository;
        this.signatureImageService = signatureImageService;
        this.certificatePdfService = certificatePdfService;
    }

    @Transactional(readOnly = true)
    public byte[] renderSignatureImageByCode(String code) {
        Signature signature = findSignatureByCode(code);
        String koreanName = resolveKoreanName(signature);
        log.info("Certificate signature image download requested. signatureId={} codeSuffix={}", signature.getId(), codeSuffix(code));
        return signatureImageService.renderStrongCalligraphyText(koreanName, SIGNATURE_IMAGE_WIDTH, SIGNATURE_IMAGE_HEIGHT);
    }

    @Transactional(readOnly = true)
    public byte[] renderCertificatePdfByCode(String code) {
        Signature signature = findSignatureByCode(code);
        String koreanName = resolveKoreanName(signature);
        String englishName = resolveEnglishName(signature);
        log.info("Certificate PDF download requested. signatureId={} codeSuffix={}", signature.getId(), codeSuffix(code));
        // 서명 이미지는 저장하지 않고 다운로드 요청마다 한글 이름 기준으로 서예 이미지화한다.
        return certificatePdfService.renderKoreanCalligraphyCertificate(englishName, koreanName, null);
    }

    private Signature findSignatureByCode(String code) {
        IdentifierCode identifierCode = identifierCodeRepository.findTopByCodeOrderByIdDesc(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.CERTIFICATE_DOWNLOAD_CODE_INVALID));
        return signatureRepository.findByUserId(identifierCode.getUser().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CERTIFICATE_SIGNATURE_NOT_FOUND));
    }

    private String resolveEnglishName(Signature signature) {
        return blankToDefault(signature.getEnglishName(), signature.getOriginalName());
    }

    private String resolveKoreanName(Signature signature) {
        String koreanName = blankToDefault(signature.getKoreanName(), signature.getKoreanText());
        return blankToDefault(koreanName, signature.getRecognizedText());
    }

    private String blankToDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue == null || defaultValue.isBlank() ? "서명" : defaultValue.trim();
        }
        return value.trim();
    }

    private String codeSuffix(String code) {
        if (code == null || code.length() < 2) {
            return "**";
        }
        return "**" + code.substring(code.length() - 2);
    }
}

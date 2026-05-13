package com.example.springboot.service;

import com.example.springboot.domain.ExhibitionSurvey;
import com.example.springboot.dto.ExhibitionSurveyRequest;
import com.example.springboot.dto.SimpleCreatedResponse;
import com.example.springboot.exception.BusinessException;
import com.example.springboot.exception.ErrorCode;
import com.example.springboot.repository.ExhibitionSurveyRepository;
import com.example.springboot.repository.IdentifierCodeRepository;
import com.example.springboot.util.PhoneUtils;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ExhibitionSurveyService {

    private final ExhibitionSurveyRepository repository;
    private final IdentifierCodeRepository identifierCodeRepository;
    private final PointRewardService pointRewardService;
    private final PhoneUtils phoneUtils;
    private final Clock clock;

    public SimpleCreatedResponse create(ExhibitionSurveyRequest request) {
        String normalizedName = request.name().trim();
        String normalizedPhone = phoneUtils.normalize(request.phoneNumber());
        
        log.info("Creating exhibition survey for user: {}, phone: {}", normalizedName, normalizedPhone);

        LocalDateTime tenMinutesAgo = LocalDateTime.now(clock).minusMinutes(10);
        boolean duplicateIn10m = repository.existsByNameAndPhoneNumberAndCreatedAtAfter(
                normalizedName, normalizedPhone, tenMinutesAgo
        );
        if (duplicateIn10m) {
            log.warn("Duplicate exhibition survey detected within 10m for: {}", normalizedPhone);
            throw new BusinessException(ErrorCode.DUPLICATE_SURVEY);
        }

        ExhibitionSurvey entity = new ExhibitionSurvey();
        entity.setName(normalizedName);
        entity.setPhoneNumber(normalizedPhone);
        entity.setAddress(request.address().trim());
        entity.setIdentifierCode(request.identifierCode());
        entity.setImpressivePoint(request.impressivePoint().trim());
        entity.setImprovementNeeded(request.improvementNeeded().trim());
        entity.setDesiredGenre(request.desiredGenre().trim());
        entity.setInvitedArtist(request.invitedArtist().trim());
        entity.setFeedback(request.feedback().trim());
        ExhibitionSurvey saved = repository.save(entity);
        log.info("Exhibition survey saved with ID: {}", saved.getId());

        // 식별 코드가 있다면 해당 유저에게 포인트 지급
        if (request.identifierCode() != null && !request.identifierCode().isBlank()) {
            identifierCodeRepository.findTopByCodeOrderByIdDesc(request.identifierCode())
                    .ifPresent(code -> {
                        log.info("Found identifier code: {}. Rewarding points to user: {}", request.identifierCode(), code.getUser().getId());
                        pointRewardService.earnActivityPoints(code.getUser().getId(), "전시 설문 참여 완료");
                    });
        }

        return new SimpleCreatedResponse(saved.getId());
    }
}

package com.example.springboot.service;

import com.example.springboot.domain.ExperienceZoneSurvey;
import com.example.springboot.dto.ExperienceZoneSurveyRequest;
import com.example.springboot.dto.SimpleCreatedResponse;
import com.example.springboot.exception.BusinessException;
import com.example.springboot.exception.ErrorCode;
import com.example.springboot.repository.ExperienceZoneSurveyRepository;
import com.example.springboot.util.PhoneUtils;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ExperienceZoneSurveyService {

    private final ExperienceZoneSurveyRepository repository;
    private final PointRewardService pointRewardService;
    private final PhoneUtils phoneUtils;
    private final Clock clock;

    public SimpleCreatedResponse create(ExperienceZoneSurveyRequest request, Long userId) {
        String normalizedName = request.name().trim();
        String normalizedPhone = phoneUtils.normalize(request.phoneNumber());
        
        LocalDateTime tenMinutesAgo = LocalDateTime.now(clock).minusMinutes(10);
        boolean duplicateIn10m = repository.existsByNameAndPhoneNumberAndCreatedAtAfter(
                normalizedName, normalizedPhone, tenMinutesAgo
        );
        if (duplicateIn10m) {
            throw new BusinessException(ErrorCode.DUPLICATE_SURVEY);
        }

        ExperienceZoneSurvey entity = new ExperienceZoneSurvey();
        entity.setName(normalizedName);
        entity.setPhoneNumber(normalizedPhone);
        entity.setAddress(request.address().trim());
        entity.setImpressiveSpace(request.impressiveSpace().trim());
        entity.setImprovementIdeaSpace(request.improvementIdeaSpace().trim());
        entity.setStreamingParticipation(request.streamingParticipation().trim());
        entity.setDesiredGoods(request.desiredGoods().trim());
        entity.setFeedback(request.feedback().trim());
        ExperienceZoneSurvey saved = repository.save(entity);

        if (userId != null) {
            pointRewardService.earnActivityPoints(userId, "체험존 설문 참여 리워드");
        }

        return new SimpleCreatedResponse(saved.getId());
    }
}

package com.example.springboot.service;

import com.example.springboot.domain.ExperienceZoneSurvey;
import com.example.springboot.dto.ExperienceZoneSurveyRequest;
import com.example.springboot.dto.SimpleCreatedResponse;
import com.example.springboot.repository.ExperienceZoneSurveyRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExperienceZoneSurveyService {

    private final ExperienceZoneSurveyRepository repository;

    public ExperienceZoneSurveyService(ExperienceZoneSurveyRepository repository) {
        this.repository = repository;
    }

    public SimpleCreatedResponse create(ExperienceZoneSurveyRequest request) {
        String normalizedName = request.name().trim();
        String normalizedPhone = normalizePhone(request.phoneNumber());
        boolean duplicateIn10m = repository.existsByNameAndPhoneNumberAndCreatedAtAfter(
                normalizedName, normalizedPhone, LocalDateTime.now().minusMinutes(10)
        );
        if (duplicateIn10m) {
            throw new IllegalArgumentException("duplicate survey: cannot submit within 10 minutes.");
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
        return new SimpleCreatedResponse(saved.getId());
    }

    private String normalizePhone(String phoneNumber) {
        String digits = phoneNumber.replaceAll("[^0-9]", "");
        if (digits.length() == 11) return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        if (digits.length() == 10) return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        return phoneNumber.trim();
    }
}


package com.example.springboot.service;

import com.example.springboot.domain.ExhibitionSurvey;
import com.example.springboot.dto.ExhibitionSurveyRequest;
import com.example.springboot.dto.SimpleCreatedResponse;
import com.example.springboot.repository.ExhibitionSurveyRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExhibitionSurveyService {

    private final ExhibitionSurveyRepository repository;

    public ExhibitionSurveyService(ExhibitionSurveyRepository repository) {
        this.repository = repository;
    }

    public SimpleCreatedResponse create(ExhibitionSurveyRequest request) {
        String normalizedName = request.name().trim();
        String normalizedPhone = normalizePhone(request.phoneNumber());
        boolean duplicateIn10m = repository.existsByNameAndPhoneNumberAndCreatedAtAfter(
                normalizedName, normalizedPhone, LocalDateTime.now().minusMinutes(10)
        );
        if (duplicateIn10m) {
            throw new IllegalArgumentException("duplicate survey: cannot submit within 10 minutes.");
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
        return new SimpleCreatedResponse(saved.getId());
    }

    private String normalizePhone(String phoneNumber) {
        String digits = phoneNumber.replaceAll("[^0-9]", "");
        if (digits.length() == 11) return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        if (digits.length() == 10) return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        return phoneNumber.trim();
    }
}


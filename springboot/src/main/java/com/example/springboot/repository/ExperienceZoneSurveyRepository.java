package com.example.springboot.repository;

import com.example.springboot.domain.ExperienceZoneSurvey;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceZoneSurveyRepository extends JpaRepository<ExperienceZoneSurvey, Long> {
    boolean existsByNameAndPhoneNumberAndCreatedAtAfter(String name, String phoneNumber, LocalDateTime createdAt);
}


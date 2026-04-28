package com.example.springboot.repository;

import com.example.springboot.domain.ExhibitionSurvey;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExhibitionSurveyRepository extends JpaRepository<ExhibitionSurvey, Long> {
    boolean existsByNameAndPhoneNumberAndCreatedAtAfter(String name, String phoneNumber, LocalDateTime createdAt);
}


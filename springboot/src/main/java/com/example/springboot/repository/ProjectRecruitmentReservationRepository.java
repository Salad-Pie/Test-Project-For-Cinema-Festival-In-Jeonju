package com.example.springboot.repository;

import com.example.springboot.domain.ProjectRecruitmentReservation;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRecruitmentReservationRepository extends JpaRepository<ProjectRecruitmentReservation, Long> {
    boolean existsByProjectKeyAndUserIdAndDateAndTime(String projectKey, Long userId, LocalDate date, LocalTime time);
}


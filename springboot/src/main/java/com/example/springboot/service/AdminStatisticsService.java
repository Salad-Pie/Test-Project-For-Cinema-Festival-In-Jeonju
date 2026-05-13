package com.example.springboot.service;

import com.example.springboot.dto.admin.AdminStatisticsResponse;
import com.example.springboot.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminStatisticsService {

    private final UserRepository userRepository;
    private final SignatureRepository signatureRepository;
    private final StreetCollaborationReservationRepository streetRepo;
    private final ExhibitionArtistMeetingReservationRepository artistRepo;
    private final ProjectRecruitmentReservationRepository projectRepo;
    private final SponsorshipApplicationRepository sponsorshipRepo;
    private final ExhibitionSurveyRepository exhibitionSurveyRepo;
    private final ExperienceZoneSurveyRepository experienceSurveyRepo;

    public AdminStatisticsService(
            UserRepository userRepository,
            SignatureRepository signatureRepository,
            StreetCollaborationReservationRepository streetRepo,
            ExhibitionArtistMeetingReservationRepository artistRepo,
            ProjectRecruitmentReservationRepository projectRepo,
            SponsorshipApplicationRepository sponsorshipRepo,
            ExhibitionSurveyRepository exhibitionSurveyRepo,
            ExperienceZoneSurveyRepository experienceSurveyRepo) {
        this.userRepository = userRepository;
        this.signatureRepository = signatureRepository;
        this.streetRepo = streetRepo;
        this.artistRepo = artistRepo;
        this.projectRepo = projectRepo;
        this.sponsorshipRepo = sponsorshipRepo;
        this.exhibitionSurveyRepo = exhibitionSurveyRepo;
        this.experienceSurveyRepo = experienceSurveyRepo;
    }

    public AdminStatisticsResponse getStatistics() {
        long totalUsers = userRepository.count();
        long totalSignatures = signatureRepository.count();

        long streetCount = streetRepo.count();
        long artistCount = artistRepo.count();
        long projectCount = projectRepo.count();
        long sponsorshipCount = sponsorshipRepo.count();
        long exhibitionSurveyCount = exhibitionSurveyRepo.count();
        long experienceSurveyCount = experienceSurveyRepo.count();

        long totalReservations = streetCount + artistCount + projectCount + sponsorshipCount;

        Map<String, Long> reservationsByType = new LinkedHashMap<>();
        reservationsByType.put("Street Collaboration", streetCount);
        reservationsByType.put("Artist Meeting", artistCount);
        reservationsByType.put("Project Recruitment", projectCount);
        reservationsByType.put("Sponsorship", sponsorshipCount);
        reservationsByType.put("Exhibition Survey", exhibitionSurveyCount);
        reservationsByType.put("Experience Zone Survey", experienceSurveyCount);

        // Language distribution from signatures
        Map<String, Long> userLocales = signatureRepository.findAll().stream()
                .filter(s -> s.getDetectedLanguage() != null)
                .collect(Collectors.groupingBy(s -> s.getDetectedLanguage().name(), Collectors.counting()));

        // OCR Confidence
        Double avgConfidence = signatureRepository.findAll().stream()
                .filter(s -> s.getOcrConfidence() != null)
                .mapToDouble(s -> s.getOcrConfidence())
                .average()
                .orElse(0.0) * 100.0; // percentage

        // Daily trend (Last 7 days)
        List<AdminStatisticsResponse.DailyCount> dailyTrend = calculateDailyTrend();

        return new AdminStatisticsResponse(
                totalUsers,
                totalSignatures,
                totalReservations,
                reservationsByType,
                userLocales,
                Math.round(avgConfidence * 10.0) / 10.0, // round to 1 decimal
                dailyTrend
        );
    }

    private List<AdminStatisticsResponse.DailyCount> calculateDailyTrend() {
        LocalDate today = LocalDate.now();
        Map<LocalDate, Long> counts = new TreeMap<>();

        for (int i = 0; i < 7; i++) {
            counts.put(today.minusDays(i), 0L);
        }

        // Simplification: We just aggregate across all for demonstration
        // In real production, this would be optimized with a native query
        aggregateReservationDates(counts);

        return counts.entrySet().stream()
                .map(e -> new AdminStatisticsResponse.DailyCount(
                        e.getKey().format(DateTimeFormatter.ISO_DATE),
                        e.getValue()))
                .collect(Collectors.toList());
    }

    private void aggregateReservationDates(Map<LocalDate, Long> counts) {
        // This is a bit heavy but works for initial implementation
        streetRepo.findAll().forEach(r -> incrementIfInRange(counts, r.getCreatedAt()));
        artistRepo.findAll().forEach(r -> incrementIfInRange(counts, r.getCreatedAt()));
        projectRepo.findAll().forEach(r -> incrementIfInRange(counts, r.getCreatedAt()));
        sponsorshipRepo.findAll().forEach(r -> incrementIfInRange(counts, r.getCreatedAt()));
    }

    private void incrementIfInRange(Map<LocalDate, Long> counts, LocalDateTime dateTime) {
        if (dateTime == null) return;
        LocalDate date = dateTime.toLocalDate();
        if (counts.containsKey(date)) {
            counts.put(date, counts.get(date) + 1);
        }
    }
}

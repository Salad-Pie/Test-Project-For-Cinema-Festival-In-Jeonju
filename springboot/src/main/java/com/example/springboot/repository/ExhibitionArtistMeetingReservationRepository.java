package com.example.springboot.repository;

import com.example.springboot.domain.ExhibitionArtistMeetingReservation;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExhibitionArtistMeetingReservationRepository extends JpaRepository<ExhibitionArtistMeetingReservation, Long> {
    boolean existsByUserIdAndDateAndTime(Long userId, LocalDate date, LocalTime time);
}

package com.example.springboot.repository;

import com.example.springboot.domain.StreetCollaborationReservation;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreetCollaborationReservationRepository extends JpaRepository<StreetCollaborationReservation, Long> {
    long countByReservationAt(LocalDateTime reservationAt);
    boolean existsByReservationAtAndNameAndPhoneNumber(LocalDateTime reservationAt, String name, String phoneNumber);
}

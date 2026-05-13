package com.example.springboot.service;

import com.example.springboot.domain.StreetCollaborationReservation;
import com.example.springboot.dto.StreetCollaborationAvailabilityResponse;
import com.example.springboot.dto.StreetCollaborationReservationRequest;
import com.example.springboot.dto.StreetCollaborationReservationResponse;
import com.example.springboot.exception.BusinessException;
import com.example.springboot.exception.ErrorCode;
import com.example.springboot.repository.StreetCollaborationReservationRepository;
import com.example.springboot.util.PhoneUtils;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StreetCollaborationReservationService {

    private static final int CAPACITY_PER_SLOT = 50;
    private final StreetCollaborationReservationRepository repository;
    private final PointRewardService pointRewardService;
    private final PhoneUtils phoneUtils;

    public StreetCollaborationReservationResponse create(StreetCollaborationReservationRequest request, Long userId) {
        validateReservationAt(request.reservationAt());
        String normalizedName = request.name().trim();
        String normalizedPhone = phoneUtils.normalize(request.phoneNumber());

        boolean alreadyReserved = repository.existsByReservationAtAndNameAndPhoneNumber(
                request.reservationAt(),
                normalizedName,
                normalizedPhone
        );
        if (alreadyReserved) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }

        StreetCollaborationAvailabilityResponse availability = getAvailability(request.reservationAt());
        if (!availability.available()) {
            throw new BusinessException(ErrorCode.CAPACITY_FULL);
        }

        StreetCollaborationReservation entity = new StreetCollaborationReservation();
        entity.setName(normalizedName);
        entity.setPhoneNumber(normalizedPhone);
        entity.setReservationAt(request.reservationAt());

        StreetCollaborationReservation saved = repository.save(entity);

        if (userId != null) {
            pointRewardService.earnActivityPoints(userId, "거리 협업 예약 참여 리워드");
        }

        return new StreetCollaborationReservationResponse(
                saved.getId(),
                saved.getName(),
                saved.getPhoneNumber(),
                saved.getReservationAt(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public StreetCollaborationAvailabilityResponse getAvailability(LocalDateTime reservationAt) {
        validateReservationAt(reservationAt);
        int current = (int) repository.countByReservationAt(reservationAt);
        int remaining = Math.max(0, CAPACITY_PER_SLOT - current);
        boolean available = current < CAPACITY_PER_SLOT;
        return new StreetCollaborationAvailabilityResponse(
                reservationAt,
                current,
                CAPACITY_PER_SLOT,
                remaining,
                available
        );
    }

    private void validateReservationAt(java.time.LocalDateTime reservationAt) {
        int hour = reservationAt.getHour();
        if (hour < 17 || hour > 22) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_SLOT);
        }
        if (reservationAt.getMinute() != 0 || reservationAt.getSecond() != 0 || reservationAt.getNano() != 0) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_SLOT);
        }
    }
}

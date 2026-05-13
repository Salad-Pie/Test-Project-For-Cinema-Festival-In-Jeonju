package com.example.springboot.service;

import com.example.springboot.domain.ExhibitionArtistMeetingReservation;
import com.example.springboot.domain.User;
import com.example.springboot.dto.ExhibitionArtistMeetingReservationRequest;
import com.example.springboot.dto.ExhibitionArtistMeetingReservationResponse;
import com.example.springboot.exception.BusinessException;
import com.example.springboot.exception.ErrorCode;
import com.example.springboot.repository.ExhibitionArtistMeetingReservationRepository;
import com.example.springboot.repository.UserRepository;
import com.example.springboot.security.JwtTokenProvider;
import com.example.springboot.security.TokenType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ExhibitionArtistMeetingReservationService {

    private static final Map<LocalDate, Set<LocalTime>> ALLOWED_SLOTS = Map.of(
            LocalDate.of(2026, 5, 2), Set.of(LocalTime.of(17, 0), LocalTime.of(19, 0)),
            LocalDate.of(2026, 5, 4), Set.of(LocalTime.of(19, 0), LocalTime.of(21, 0))
    );

    private final ExhibitionArtistMeetingReservationRepository repository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PointRewardService pointRewardService;

    public ExhibitionArtistMeetingReservationResponse create(
            String authorization,
            ExhibitionArtistMeetingReservationRequest request
    ) {
        String token = extractBearerToken(authorization);
        TokenType tokenType = jwtTokenProvider.extractTokenType(token);
        if (tokenType != TokenType.REGISTER && tokenType != TokenType.VERIFIED) {
            throw new IllegalArgumentException("token type is not valid for exhibition artist meeting.");
        }

        validateAllowedSlot(request.date(), request.time());

        Long userId = jwtTokenProvider.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found."));

        boolean alreadyReserved = repository.existsByUserIdAndDateAndTime(userId, request.date(), request.time());
        if (alreadyReserved) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }

        ExhibitionArtistMeetingReservation entity = new ExhibitionArtistMeetingReservation();
        entity.setUser(user);
        entity.setDate(request.date());
        entity.setTime(request.time());

        ExhibitionArtistMeetingReservation saved = repository.save(entity);
        pointRewardService.earnActivityPoints(userId, "작가와의 만남 예약 신청 완료");
        return new ExhibitionArtistMeetingReservationResponse(
                saved.getId(),
                saved.getUser().getId(),
                saved.getDate(),
                saved.getTime(),
                saved.getCreatedAt()
        );
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("authorization bearer token is required.");
        }
        return authorization.substring("Bearer ".length()).trim();
    }

    private void validateAllowedSlot(LocalDate date, LocalTime time) {
        Set<LocalTime> allowedTimes = ALLOWED_SLOTS.get(date);
        if (allowedTimes == null || !allowedTimes.contains(time)) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_SLOT);
        }
    }
}

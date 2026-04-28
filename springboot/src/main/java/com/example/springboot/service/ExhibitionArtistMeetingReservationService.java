package com.example.springboot.service;

import com.example.springboot.domain.ExhibitionArtistMeetingReservation;
import com.example.springboot.domain.User;
import com.example.springboot.dto.ExhibitionArtistMeetingReservationRequest;
import com.example.springboot.dto.ExhibitionArtistMeetingReservationResponse;
import com.example.springboot.repository.ExhibitionArtistMeetingReservationRepository;
import com.example.springboot.repository.UserRepository;
import com.example.springboot.security.JwtTokenProvider;
import com.example.springboot.security.TokenType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExhibitionArtistMeetingReservationService {

    private static final Map<LocalDate, Set<LocalTime>> ALLOWED_SLOTS = Map.of(
            LocalDate.of(2026, 5, 2), Set.of(LocalTime.of(17, 0), LocalTime.of(19, 0)),
            LocalDate.of(2026, 5, 4), Set.of(LocalTime.of(19, 0), LocalTime.of(21, 0))
    );

    private final ExhibitionArtistMeetingReservationRepository repository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public ExhibitionArtistMeetingReservationService(
            ExhibitionArtistMeetingReservationRepository repository,
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

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
            throw new IllegalArgumentException("duplicate reservation: same user already reserved this slot.");
        }

        ExhibitionArtistMeetingReservation entity = new ExhibitionArtistMeetingReservation();
        entity.setUser(user);
        entity.setDate(request.date());
        entity.setTime(request.time());

        ExhibitionArtistMeetingReservation saved = repository.save(entity);
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
            throw new IllegalArgumentException("reservation is available only for predefined exhibition artist meeting slots.");
        }
    }
}

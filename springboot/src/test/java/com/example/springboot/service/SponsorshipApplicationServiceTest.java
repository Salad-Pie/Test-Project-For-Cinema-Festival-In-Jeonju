package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.springboot.domain.SponsorshipApplication;
import com.example.springboot.dto.SponsorshipApplicationRequest;
import com.example.springboot.dto.SponsorshipApplicationResponse;
import com.example.springboot.exception.BusinessException;
import com.example.springboot.repository.SponsorshipApplicationRepository;
import com.example.springboot.util.BankAccountUtils;
import com.example.springboot.util.PhoneUtils;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SponsorshipApplicationServiceTest {

    private SponsorshipApplicationService service;

    @Mock private SponsorshipApplicationRepository repository;
    @Mock private DataEncryptionService dataEncryptionService;
    @Mock private DataHashService dataHashService;
    @Mock private PointRewardService pointRewardService;
    
    private final PhoneUtils phoneUtils = new PhoneUtils();
    private final BankAccountUtils bankAccountUtils = new BankAccountUtils();
    
    // Fixed clock for testing
    private final Clock clock = Clock.fixed(Instant.parse("2026-05-13T10:00:00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        service = new SponsorshipApplicationService(
                repository, dataEncryptionService, dataHashService, pointRewardService, phoneUtils, bankAccountUtils, clock
        );
    }

    @Test
    @DisplayName("후원 신청 성공")
    void create_Success() {
        // given
        SponsorshipApplicationRequest request = new SponsorshipApplicationRequest(
                "Donator", "010-1111-2222", "123-456-789", "CARD", "Test Bank", 10000L, "Seoul"
        );
        
        when(dataHashService.sha256(anyString())).thenReturn("hashed-account");
        when(repository.existsByBankAccountHashAndCreatedAtAfter(eq("hashed-account"), any(LocalDateTime.class)))
                .thenReturn(false);
        when(dataEncryptionService.encrypt(anyString())).thenReturn("encrypted-account");
        
        SponsorshipApplication saved = SponsorshipApplication.builder()
                .id(1L)
                .name("Donator")
                .phoneNumber("010-1111-2222")
                .bankAccountMasked("****-****-9789")
                .paymentMethodType("CARD")
                .paymentProviderName("Test Bank")
                .amount(10000L)
                .address("Seoul")
                .build();
        
        when(repository.save(any(SponsorshipApplication.class))).thenReturn(saved);

        // when
        SponsorshipApplicationResponse response = service.create(request, 123L);

        // then
        assertNotNull(response);
        assertEquals(1L, response.id());
        verify(pointRewardService).earnActivityPoints(eq(123L), anyString());
    }

    @Test
    @DisplayName("중복 후원 신청 시 예외 발생 (10분 이내)")
    void create_Duplicate() {
        // given
        SponsorshipApplicationRequest request = new SponsorshipApplicationRequest(
                "Donator", "010-1111-2222", "123-456-789", "CARD", "Test Bank", 10000L, "Seoul"
        );
        
        when(dataHashService.sha256(anyString())).thenReturn("hashed-account");
        when(repository.existsByBankAccountHashAndCreatedAtAfter(eq("hashed-account"), any(LocalDateTime.class)))
                .thenReturn(true);

        // when & then
        assertThrows(BusinessException.class, () -> service.create(request, null));
    }
}

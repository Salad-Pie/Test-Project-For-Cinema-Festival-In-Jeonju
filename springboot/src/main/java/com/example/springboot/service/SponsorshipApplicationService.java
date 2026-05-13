package com.example.springboot.service;

import com.example.springboot.domain.SponsorshipApplication;
import com.example.springboot.dto.SponsorshipApplicationRequest;
import com.example.springboot.dto.SponsorshipApplicationResponse;
import com.example.springboot.exception.BusinessException;
import com.example.springboot.exception.ErrorCode;
import com.example.springboot.repository.SponsorshipApplicationRepository;
import com.example.springboot.util.BankAccountUtils;
import com.example.springboot.util.PhoneUtils;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SponsorshipApplicationService {

    private final SponsorshipApplicationRepository repository;
    private final DataEncryptionService dataEncryptionService;
    private final DataHashService dataHashService;
    private final PointRewardService pointRewardService;
    private final PhoneUtils phoneUtils;
    private final BankAccountUtils bankAccountUtils;
    private final Clock clock;

    public SponsorshipApplicationResponse create(SponsorshipApplicationRequest request, Long userId) {
        String normalizedName = request.name().trim();
        String normalizedPhone = phoneUtils.normalize(request.phoneNumber());
        String normalizedBankAccount = bankAccountUtils.normalize(request.bankAccount());
        String normalizedAddress = request.address().trim();
        String paymentMethodType = request.paymentMethodType().trim().toUpperCase();
        String paymentProviderName = request.paymentProviderName().trim();
        String paymentIdentity = String.join("|", paymentMethodType, paymentProviderName, normalizedBankAccount);
        String bankAccountHash = dataHashService.sha256(paymentIdentity);

        LocalDateTime tenMinutesAgo = LocalDateTime.now(clock).minusMinutes(10);
        boolean duplicated = repository.existsByBankAccountHashAndCreatedAtAfter(bankAccountHash, tenMinutesAgo);
        if (duplicated) {
            throw new BusinessException(ErrorCode.DUPLICATE_APPLICATION);
        }

        SponsorshipApplication entity = new SponsorshipApplication();
        entity.setName(normalizedName);
        entity.setPhoneNumber(normalizedPhone);
        entity.setBankAccount(dataEncryptionService.encrypt(normalizedBankAccount));
        entity.setPaymentMethodType(paymentMethodType);
        entity.setPaymentProviderName(paymentProviderName);
        entity.setBankAccountMasked(bankAccountUtils.mask(normalizedBankAccount));
        entity.setBankAccountHash(bankAccountHash);
        entity.setAmount(request.amount());
        entity.setAddress(normalizedAddress);

        SponsorshipApplication saved = repository.save(entity);
        
        if (userId != null) {
            pointRewardService.earnActivityPoints(userId, "후원 신청 참여 리워드");
        }

        return new SponsorshipApplicationResponse(
                saved.getId(),
                saved.getName(),
                saved.getPhoneNumber(),
                saved.getBankAccountMasked(),
                saved.getPaymentMethodType(),
                saved.getPaymentProviderName(),
                saved.getAmount(),
                saved.getAddress()
        );
    }
}

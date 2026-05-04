package com.example.springboot.service;

import com.example.springboot.domain.SponsorshipApplication;
import com.example.springboot.dto.SponsorshipApplicationRequest;
import com.example.springboot.dto.SponsorshipApplicationResponse;
import com.example.springboot.exception.BusinessException;
import com.example.springboot.exception.ErrorCode;
import com.example.springboot.repository.SponsorshipApplicationRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SponsorshipApplicationService {

    private final SponsorshipApplicationRepository repository;
    private final DataEncryptionService dataEncryptionService;
    private final DataHashService dataHashService;

    public SponsorshipApplicationService(
            SponsorshipApplicationRepository repository,
            DataEncryptionService dataEncryptionService,
            DataHashService dataHashService
    ) {
        this.repository = repository;
        this.dataEncryptionService = dataEncryptionService;
        this.dataHashService = dataHashService;
    }

    public SponsorshipApplicationResponse create(SponsorshipApplicationRequest request) {
        String normalizedName = request.name().trim();
        String normalizedPhone = normalizePhone(request.phoneNumber());
        String normalizedBankAccount = normalizeBankAccount(request.bankAccount());
        String normalizedAddress = request.address().trim();
        String paymentMethodType = request.paymentMethodType().trim().toUpperCase();
        String paymentProviderName = request.paymentProviderName().trim();
        String paymentIdentity = String.join("|", paymentMethodType, paymentProviderName, normalizedBankAccount);
        String bankAccountHash = dataHashService.sha256(paymentIdentity);

        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
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
        entity.setBankAccountMasked(maskBankAccount(normalizedBankAccount));
        entity.setBankAccountHash(bankAccountHash);
        entity.setAmount(request.amount());
        entity.setAddress(normalizedAddress);

        SponsorshipApplication saved = repository.save(entity);
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

    private String normalizePhone(String phoneNumber) {
        String digits = phoneNumber.replaceAll("[^0-9]", "");
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        }
        if (digits.length() == 10) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        }
        return phoneNumber.trim();
    }

    private String normalizeBankAccount(String bankAccount) {
        return bankAccount.replaceAll("\\s+", "").trim();
    }

    private String maskBankAccount(String bankAccount) {
        if (bankAccount.length() <= 4) {
            return "****";
        }
        String tail = bankAccount.substring(bankAccount.length() - 4);
        return "****-****-" + tail;
    }
}

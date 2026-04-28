package com.example.springboot.repository;

import com.example.springboot.domain.SponsorshipApplication;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SponsorshipApplicationRepository extends JpaRepository<SponsorshipApplication, Long> {
    boolean existsByBankAccountHashAndCreatedAtAfter(String bankAccountHash, LocalDateTime createdAtAfter);
}

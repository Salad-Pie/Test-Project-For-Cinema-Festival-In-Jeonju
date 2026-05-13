package com.example.springboot.repository;

import com.example.springboot.domain.Signature;
import com.example.springboot.domain.OcrStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignatureRepository extends JpaRepository<Signature, Long> {
    Optional<Signature> findByUserId(Long userId);
    List<Signature> findByOcrConfidenceLessThan(Double threshold);
    List<Signature> findByOcrStatus(OcrStatus ocrStatus);
    List<Signature> findByCreatedAtAfter(java.time.LocalDateTime dateTime);
}

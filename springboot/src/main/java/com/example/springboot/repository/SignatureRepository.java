package com.example.springboot.repository;

import com.example.springboot.domain.Signature;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignatureRepository extends JpaRepository<Signature, Long> {
    Optional<Signature> findByUserId(Long userId);
}

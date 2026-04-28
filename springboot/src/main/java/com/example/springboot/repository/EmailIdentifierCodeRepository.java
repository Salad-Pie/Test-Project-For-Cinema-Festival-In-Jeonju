package com.example.springboot.repository;

import com.example.springboot.domain.EmailIdentifierCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailIdentifierCodeRepository extends JpaRepository<EmailIdentifierCode, Long> {
    Optional<EmailIdentifierCode> findByEmail(String email);
}

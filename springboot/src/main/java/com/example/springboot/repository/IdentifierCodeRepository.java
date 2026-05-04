package com.example.springboot.repository;

import com.example.springboot.domain.IdentifierCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentifierCodeRepository extends JpaRepository<IdentifierCode, Long> {
    Optional<IdentifierCode> findTopByUserIdOrderByIdDesc(Long userId);
    Optional<IdentifierCode> findTopByCodeOrderByIdDesc(String code);
}

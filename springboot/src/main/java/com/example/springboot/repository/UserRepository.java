package com.example.springboot.repository;

import com.example.springboot.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findFirstByEmail(String email);
    Optional<User> findFirstByPhoneNumber(String phoneNumber);
    List<User> findByCreatedAtAfter(LocalDateTime threshold);
}

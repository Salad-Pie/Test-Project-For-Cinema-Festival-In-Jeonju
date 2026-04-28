package com.example.springboot.repository;

import com.example.springboot.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findFirstByEmail(String email);
    Optional<User> findFirstByPhoneNumber(String phoneNumber);
}

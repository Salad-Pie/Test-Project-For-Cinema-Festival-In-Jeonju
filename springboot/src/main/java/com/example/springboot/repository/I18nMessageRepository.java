package com.example.springboot.repository;

import com.example.springboot.domain.I18nMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface I18nMessageRepository extends JpaRepository<I18nMessage, Long> {
    List<I18nMessage> findByLocale(String locale);
    Optional<I18nMessage> findByMessageKeyAndLocale(String messageKey, String locale);
}

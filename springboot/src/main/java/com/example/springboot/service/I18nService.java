package com.example.springboot.service;

import com.example.springboot.domain.I18nMessage;
import com.example.springboot.repository.I18nMessageRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class I18nService {

    private final I18nMessageRepository repository;

    public I18nService(I18nMessageRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "i18n", key = "#locale")
    @Transactional(readOnly = true)
    public Map<String, String> getMessages(String locale) {
        List<I18nMessage> messages = repository.findByLocale(locale);
        Map<String, String> map = new HashMap<>();
        for (I18nMessage msg : messages) {
            map.put(msg.getMessageKey(), msg.getContent());
        }
        return map;
    }

    @CacheEvict(value = "i18n", key = "#locale")
    public void updateMessage(String key, String locale, String content) {
        I18nMessage msg = repository.findByMessageKeyAndLocale(key, locale)
                .orElse(new I18nMessage());
        
        msg.setMessageKey(key);
        msg.setLocale(locale);
        msg.setContent(content);
        
        repository.save(msg);
    }

    @Transactional(readOnly = true)
    public List<I18nMessage> getAllMessages() {
        return repository.findAll();
    }
}

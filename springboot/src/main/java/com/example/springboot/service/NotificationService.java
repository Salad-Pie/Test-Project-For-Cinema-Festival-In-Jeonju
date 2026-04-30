package com.example.springboot.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final RestClient restClient;

    private final String twilioAccountSid;
    private final String twilioAuthToken;
    private final String twilioFromNumber;

    public NotificationService(
            JavaMailSender mailSender,
            @Value("${app.sms.twilio.account-sid:}") String twilioAccountSid,
            @Value("${app.sms.twilio.auth-token:}") String twilioAuthToken,
            @Value("${app.sms.twilio.from-number:}") String twilioFromNumber
    ) {
        this.mailSender = mailSender;
        this.twilioAccountSid = twilioAccountSid;
        this.twilioAuthToken = twilioAuthToken;
        this.twilioFromNumber = twilioFromNumber;
        this.restClient = RestClient.builder().build();
    }

    public void sendEmailCode(String email, String code, String language) {
        if (email == null || email.isBlank()) {
            return;
        }

        EmailCodeMessage message = emailCodeMessage(language, code);
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject(message.subject());
        mailMessage.setText(message.body());
        mailSender.send(mailMessage);
    }

    public void sendSmsCode(String phoneNumber, String code) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return;
        }

        if (twilioAccountSid.isBlank() || twilioAuthToken.isBlank() || twilioFromNumber.isBlank()) {
            throw new IllegalStateException("Twilio SMS is not configured. Set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_FROM_NUMBER.");
        }

        String normalizedTo = normalizeRecipientPhone(phoneNumber);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("To", normalizedTo);
        form.add("From", twilioFromNumber);
        form.add("Body", "Your verification code is: " + code);

        String credential = twilioAccountSid + ":" + twilioAuthToken;
        String authorization = "Basic " + Base64.getEncoder().encodeToString(credential.getBytes(StandardCharsets.UTF_8));

        restClient.post()
                .uri("https://api.twilio.com/2010-04-01/Accounts/{sid}/Messages.json", twilioAccountSid)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();

        log.info("SMS sent via Twilio to {}", normalizedTo);
    }

    private String normalizeRecipientPhone(String phoneNumber) {
        String digits = phoneNumber.replaceAll("[^0-9+]", "");
        if (digits.startsWith("+")) {
            return digits;
        }

        // KR local mobile format: 010xxxxxxxx -> +8210xxxxxxxx
        if (digits.startsWith("010") && digits.length() == 11) {
            return "+82" + digits.substring(1);
        }

        // KR domestic format with leading 0 (fallback)
        if (digits.startsWith("0")) {
            return "+82" + digits.substring(1);
        }

        // If country code is already present without '+', add it.
        if (digits.startsWith("82")) {
            return "+" + digits;
        }

        throw new IllegalArgumentException("phoneNumber must be E.164 or KR domestic format.");
    }

    private EmailCodeMessage emailCodeMessage(String language, String code) {
        return switch (language) {
            case "en" -> new EmailCodeMessage(
                    "BackToScreen Verification Code",
                    "Your verification code is: " + code
            );
            case "zh" -> new EmailCodeMessage(
                    "BackToScreen 验证码",
                    "您的验证码是：" + code
            );
            case "ja" -> new EmailCodeMessage(
                    "BackToScreen 認証コード",
                    "認証コードは " + code + " です。"
            );
            default -> new EmailCodeMessage(
                    "BackToScreen 인증 코드",
                    "인증 코드는 " + code + " 입니다."
            );
        };
    }

    private record EmailCodeMessage(String subject, String body) {
    }
}

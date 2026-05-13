package com.example.springboot.util;

import org.springframework.stereotype.Component;

@Component
public class PhoneUtils {

    /**
     * 전화번호를 표준 형식(010-XXXX-XXXX)으로 변환합니다.
     */
    public String normalize(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return "";
        }
        String digits = phoneNumber.replaceAll("[^0-9]", "");
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        }
        if (digits.length() == 10) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        }
        return phoneNumber.trim();
    }
}

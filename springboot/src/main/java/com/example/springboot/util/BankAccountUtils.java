package com.example.springboot.util;

import org.springframework.stereotype.Component;

@Component
public class BankAccountUtils {

    public String normalize(String bankAccount) {
        if (bankAccount == null) return "";
        return bankAccount.replaceAll("\\s+", "").trim();
    }

    public String mask(String bankAccount) {
        if (bankAccount == null || bankAccount.length() <= 4) {
            return "****";
        }
        String tail = bankAccount.substring(bankAccount.length() - 4);
        return "****-****-" + tail;
    }
}

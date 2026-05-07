package com.example.springboot.service;

import com.example.springboot.domain.NameConversionSource;
import com.example.springboot.domain.NameLanguage;
import com.ibm.icu.text.Transliterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class EnglishKoreanNameService {

    private static final Transliterator TRANSLITERATOR = Transliterator.getInstance("Latin-Hangul");

    private static final Map<String, String> PRIORITY_NAME_MAP = Map.ofEntries(
            Map.entry("ANITA", "\uC544\uB2C8\uD0C0"),
            Map.entry("ANDAYANI", "\uC548\uB2E4\uC57C\uB2C8"),
            Map.entry("LUKFINTIA", "\uB8E8\uD06C\uD540\uD2F0\uC544"),
            Map.entry("FILIA", "\uD544\uB9AC\uC544"),
            Map.entry("RAHMAT", "\uB77C\uD750\uB9DB"),
            Map.entry("MUTAZIM", "\uBB34\uD0C0\uC9D0"),
            Map.entry("EKA", "\uC5D0\uCE74"),
            Map.entry("PUTRA", "\uD478\uD2B8\uB77C"),
            Map.entry("SILVANA", "\uC2E4\uBC14\uB098"),
            Map.entry("RESKI", "\uB808\uC2A4\uD0A4"),
            Map.entry("MULIAWAN", "\uBB3C\uB9AC\uC544\uC644"),
            Map.entry("MARWIYA", "\uB9C8\uB974\uC704\uC57C"),
            Map.entry("ARIFUDDIN", "\uC544\uB9AC\uD478\uB518"),
            Map.entry("TIMBANG", "\uD300\uBC29"),
            Map.entry("MAYRIDA", "\uB9C8\uC774\uB9AC\uB2E4"),
            Map.entry("NUDE", "\uB204\uB370"),
            Map.entry("HAMMA", "\uD568\uB9C8"),
            Map.entry("SUCI", "\uC218\uCE58"),
            Map.entry("PRATIWI", "\uD504\uB77C\uD2F0\uC704"),
            Map.entry("MANGANTAR", "\uB9DD\uC548\uD0C0\uB974"),
            Map.entry("EVI", "\uC5D0\uBE44"),
            Map.entry("SULTRIANA", "\uC220\uD2B8\uB9AC\uC544\uB098"),
            Map.entry("ZACHRI", "\uC790\uD06C\uB9AC"),
            Map.entry("LENDA", "\uB80C\uB2E4"),
            Map.entry("LIEM", "\uB9AC\uC5E0"),
            Map.entry("AULIA", "\uC544\uC6B8\uB9AC\uC544"),
            Map.entry("ENJELINA", "\uC5D4\uC824\uB9AC\uB098"),
            Map.entry("RAUFIKA", "\uB77C\uC6B0\uD53C\uCE74"),
            Map.entry("SUNARYO", "\uC218\uB098\uB974\uC694"),
            Map.entry("YUNITA", "\uC720\uB2C8\uD0C0"),
            Map.entry("MANDRASARI", "\uB9CC\uB4DC\uB77C\uC0AC\uB9AC"),
            Map.entry("YULIATI", "\uC728\uB9AC\uC544\uD2F0"),
            Map.entry("NANIK", "\uB098\uB2C9"),
            Map.entry("HARIANI", "\uD558\uB9AC\uC544\uB2C8"),
            Map.entry("RIVANA", "\uB9AC\uBC14\uB098"),
            Map.entry("AMELIA", "\uC544\uBA5C\uB9AC\uC544"),
            Map.entry("FIONITA", "\uD53C\uC624\uB2C8\uD0C0"),
            Map.entry("HADI", "\uD558\uB514"),
            Map.entry("HANDRIAN", "\uD55C\uB4DC\uB9AC\uC548"),
            Map.entry("KANG", "\uAC15"),
            Map.entry("BONGJU", "\uBD09\uC8FC")
    );

    private static final Map<String, String> OFFICIAL_EXAMPLE_DICTIONARY = Map.ofEntries(
            Map.entry("alexander", "\uC54C\uB809\uC0B0\uB354"),
            Map.entry("amelia", "\uC544\uBA5C\uB9AC\uC544"),
            Map.entry("andrew", "\uC564\uB4DC\uB8E8"),
            Map.entry("anna", "\uC548\uB098"),
            Map.entry("anthony", "\uC564\uD1A0\uB2C8"),
            Map.entry("benjamin", "\uBC24\uC790\uBBFC"),
            Map.entry("charles", "\uCC30\uC2A4"),
            Map.entry("christopher", "\uD06C\uB9AC\uC2A4\uD1A0\uD37C"),
            Map.entry("daniel", "\uB2E4\uB2C8\uC5D8"),
            Map.entry("danielle", "\uB2E4\uB2C8\uC5D8"),
            Map.entry("david", "\uB370\uC774\uBE44\uB4DC"),
            Map.entry("edward", "\uC5D0\uB4DC\uC6CC\uB4DC"),
            Map.entry("elizabeth", "\uC5D8\uB9AC\uC790\uBCA0\uC2A4"),
            Map.entry("emily", "\uC5D0\uBC00\uB9AC"),
            Map.entry("emma", "\uC5E0\uB9C8"),
            Map.entry("ethan", "\uC774\uB4E0"),
            Map.entry("george", "\uC870\uC9C0"),
            Map.entry("grace", "\uADF8\uB808\uC774\uC2A4"),
            Map.entry("henry", "\uD5E8\uB9AC"),
            Map.entry("isabella", "\uC774\uC0AC\uBCA8\uB77C"),
            Map.entry("jacob", "\uC81C\uC774\uCF65"),
            Map.entry("james", "\uC81C\uC784\uC2A4"),
            Map.entry("jennifer", "\uC81C\uB2C8\uD37C"),
            Map.entry("john", "\uC874"),
            Map.entry("johnson", "\uC874\uC2A8"),
            Map.entry("joseph", "\uC870\uC138\uD504"),
            Map.entry("joshua", "\uC870\uC218\uC544"),
            Map.entry("kevin", "\uCF00\uBE48"),
            Map.entry("laura", "\uB85C\uB77C"),
            Map.entry("liam", "\uB9AC\uC5C4"),
            Map.entry("lucas", "\uB8E8\uCE74\uC2A4"),
            Map.entry("martin", "\uB9C8\uD2F4"),
            Map.entry("mary", "\uBA54\uB9AC"),
            Map.entry("michael", "\uB9C8\uC774\uD074"),
            Map.entry("olivia", "\uC62C\uB9AC\uBE44\uC544"),
            Map.entry("paul", "\uD3F4"),
            Map.entry("peter", "\uD53C\uD130"),
            Map.entry("richard", "\uB9AC\uCC98\uB4DC"),
            Map.entry("robert", "\uB85C\uBC84\uD2B8"),
            Map.entry("sarah", "\uC138\uB77C"),
            Map.entry("smith", "\uC2A4\uBBF8\uC2A4"),
            Map.entry("sofia", "\uC18C\uD53C\uC544"),
            Map.entry("sophia", "\uC18C\uD53C\uC544"),
            Map.entry("thomas", "\uD1A0\uB9C8\uC2A4"),
            Map.entry("william", "\uC708\uB9AC\uC5C4")
    );

    public String toKoreanPronunciation(String value) {
        return convertByOfficialLoanwordPolicy(value, NameLanguage.EN).koreanName();
    }

    public ConversionResult convertByOfficialLoanwordPolicy(String value) {
        return convertByOfficialLoanwordPolicy(value, NameLanguage.EN);
    }

    public ConversionResult convertByOfficialLoanwordPolicy(String value, NameLanguage language) {
        if (language != NameLanguage.EN) {
            String normalizedNonEnglish = normalizeNonEnglish(value);
            return new ConversionResult(normalizedNonEnglish, NameConversionSource.MANUAL_REVIEW);
        }

        String priorityKey = normalizePriorityKey(value);
        if (priorityKey == null) {
            return new ConversionResult(null, null);
        }

        String normalized = normalize(value);
        if (normalized == null) {
            return new ConversionResult(null, null);
        }

        StringBuilder result = new StringBuilder();
        NameConversionSource source = NameConversionSource.NIKL_EXAMPLE;
        String[] priorityWords = priorityKey.split("\\s+");
        String[] normalizedWords = normalized.split("\\s+");
        for (int index = 0; index < normalizedWords.length; index++) {
            String word = normalizedWords[index];
            if (word.isBlank()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            String priorityWord = index < priorityWords.length ? priorityWords[index] : "";
            String mappedName = PRIORITY_NAME_MAP.get(priorityWord);
            if (mappedName != null) {
                source = NameConversionSource.MANUAL;
                result.append(mappedName);
                continue;
            }
            String officialExample = OFFICIAL_EXAMPLE_DICTIONARY.get(word);
            if (officialExample == null) {
                if (source != NameConversionSource.MANUAL) {
                    source = NameConversionSource.LOANWORD_RULE;
                }
                result.append(TRANSLITERATOR.transliterate(word));
            } else {
                result.append(officialExample);
            }
        }
        return new ConversionResult(result.toString(), source);
    }

    public Map<String, String> getPriorityNameMap() {
        return new LinkedHashMap<>(PRIORITY_NAME_MAP);
    }

    private String normalizePriorityKey(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.toUpperCase(Locale.ROOT)
                .replace('\u2019', '\'')
                .replace('`', '\'')
                .replaceAll("'", "")
                .replaceAll("[^A-Z\\s-]", " ")
                .replace('-', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT)
                .replace('\u2019', '\'')
                .replace('`', '\'')
                .replaceAll("'", "")
                .replaceAll("[^a-z\\s-]", " ")
                .replace('-', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalizeNonEnglish(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.replaceAll("\\s+", " ").trim();
    }

    public record ConversionResult(String koreanName, NameConversionSource source) {
    }
}

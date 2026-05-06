package com.example.springboot.service;

import com.example.springboot.domain.NameConversionSource;
import com.example.springboot.domain.NameLanguage;
import java.util.Map;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class EnglishKoreanNameService {

    private static final char HANGUL_BASE = 0xAC00;
    private static final int VOWEL_COUNT = 21;
    private static final int FINAL_COUNT = 28;

    private static final String[] INITIALS = {
            "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ",
            "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    };
    private static final String[] VOWELS = {
            "ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ",
            "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"
    };
    private static final String[] FINALS = {
            "", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ",
            "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ",
            "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    };
    private static final Map<String, String> OFFICIAL_EXAMPLE_DICTIONARY = Map.ofEntries(
            Map.entry("alexander", "알렉산더"),
            Map.entry("andrew", "앤드루"),
            Map.entry("anthony", "앤서니"),
            Map.entry("charles", "찰스"),
            Map.entry("christopher", "크리스토퍼"),
            Map.entry("daniel", "대니얼"),
            Map.entry("david", "데이비드"),
            Map.entry("elizabeth", "엘리자베스"),
            Map.entry("george", "조지"),
            Map.entry("henry", "헨리"),
            Map.entry("james", "제임스"),
            Map.entry("john", "존"),
            Map.entry("johnson", "존슨"),
            Map.entry("michael", "마이클"),
            Map.entry("paul", "폴"),
            Map.entry("richard", "리처드"),
            Map.entry("robert", "로버트"),
            Map.entry("smith", "스미스"),
            Map.entry("william", "윌리엄")
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

        String normalized = normalize(value);
        if (normalized == null) {
            return new ConversionResult(null, null);
        }

        StringBuilder result = new StringBuilder();
        NameConversionSource source = NameConversionSource.NIKL_EXAMPLE;
        for (String word : normalized.split("\\s+")) {
            if (word.isBlank()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            String officialExample = OFFICIAL_EXAMPLE_DICTIONARY.get(word);
            if (officialExample == null) {
                source = NameConversionSource.LOANWORD_RULE;
                result.append(convertWord(word));
            } else {
                result.append(officialExample);
            }
        }
        return new ConversionResult(result.toString(), source);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z\\s-]", " ").replace('-', ' ').trim();
    }

    private String normalizeNonEnglish(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.replaceAll("\\s+", " ").trim();
    }

    private String convertWord(String word) {
        StringBuilder result = new StringBuilder();
        int index = 0;
        while (index < word.length()) {
            Segment segment = nextSegment(word, index);
            result.append(compose(segment.initial, segment.vowel, segment.fin));
            index = segment.nextIndex;
        }
        return result.toString();
    }

    private Segment nextSegment(String word, int index) {
        Initial initial = readInitial(word, index);
        int cursor = initial.nextIndex;

        Vowel vowel = readVowel(word, cursor);
        if (vowel == null) {
            return new Segment(initial.value, "ㅡ", "", cursor);
        }
        cursor = vowel.nextIndex;

        String fin = "";
        if (cursor < word.length() - 1 && !isVowelStart(word, cursor)) {
            Initial nextConsonant = readInitial(word, cursor);
            if (nextConsonant.nextIndex < word.length() && isVowelStart(word, nextConsonant.nextIndex)) {
                return new Segment(initial.value, vowel.value, "", cursor);
            }
            fin = toFinal(nextConsonant.value);
            cursor = nextConsonant.nextIndex;
        } else if (cursor == word.length() - 1 && !isVowelStart(word, cursor)) {
            Initial lastConsonant = readInitial(word, cursor);
            fin = toFinal(lastConsonant.value);
            cursor = lastConsonant.nextIndex;
        }

        return new Segment(initial.value, vowel.value, fin, cursor);
    }

    private Initial readInitial(String word, int index) {
        if (index >= word.length()) {
            return new Initial("ㅇ", index);
        }
        String remaining = word.substring(index);
        if (remaining.startsWith("ch")) {
            return new Initial("ㅊ", index + 2);
        }
        if (remaining.startsWith("sh")) {
            return new Initial("ㅅ", index + 2);
        }
        if (remaining.startsWith("ph")) {
            return new Initial("ㅍ", index + 2);
        }
        if (remaining.startsWith("th")) {
            return new Initial("ㅅ", index + 2);
        }

        return switch (word.charAt(index)) {
            case 'b', 'v' -> new Initial("ㅂ", index + 1);
            case 'c', 'k', 'q' -> new Initial("ㅋ", index + 1);
            case 'd' -> new Initial("ㄷ", index + 1);
            case 'f' -> new Initial("ㅍ", index + 1);
            case 'g' -> new Initial("ㄱ", index + 1);
            case 'h' -> new Initial("ㅎ", index + 1);
            case 'j', 'z' -> new Initial("ㅈ", index + 1);
            case 'l', 'r' -> new Initial("ㄹ", index + 1);
            case 'm' -> new Initial("ㅁ", index + 1);
            case 'n' -> new Initial("ㄴ", index + 1);
            case 'p' -> new Initial("ㅍ", index + 1);
            case 's', 'x' -> new Initial("ㅅ", index + 1);
            case 't' -> new Initial("ㅌ", index + 1);
            case 'w' -> new Initial("ㅇ", index + 1);
            case 'y' -> new Initial("ㅇ", index + 1);
            default -> new Initial("ㅇ", index);
        };
    }

    private Vowel readVowel(String word, int index) {
        if (index >= word.length()) {
            return new Vowel("ㅡ", index);
        }
        String remaining = word.substring(index);
        if (remaining.startsWith("ae")) {
            return new Vowel("ㅐ", index + 2);
        }
        if (remaining.startsWith("ai") || remaining.startsWith("ay")) {
            return new Vowel("ㅔ", index + 2);
        }
        if (remaining.startsWith("ee") || remaining.startsWith("ea")) {
            return new Vowel("ㅣ", index + 2);
        }
        if (remaining.startsWith("oo")) {
            return new Vowel("ㅜ", index + 2);
        }
        if (remaining.startsWith("ou") || remaining.startsWith("ow")) {
            return new Vowel("ㅏ", index + 2);
        }

        return switch (word.charAt(index)) {
            case 'a' -> new Vowel("ㅏ", index + 1);
            case 'e' -> new Vowel("ㅔ", index + 1);
            case 'i', 'y' -> new Vowel("ㅣ", index + 1);
            case 'o' -> new Vowel("ㅗ", index + 1);
            case 'u' -> new Vowel("ㅜ", index + 1);
            default -> null;
        };
    }

    private boolean isVowelStart(String word, int index) {
        if (index >= word.length()) {
            return false;
        }
        return "aeiouy".indexOf(word.charAt(index)) >= 0;
    }

    private String toFinal(String initial) {
        return switch (initial) {
            case "ㄱ", "ㅋ" -> "ㄱ";
            case "ㄴ" -> "ㄴ";
            case "ㄷ", "ㅌ", "ㅅ", "ㅈ", "ㅊ" -> "ㅅ";
            case "ㄹ" -> "ㄹ";
            case "ㅁ" -> "ㅁ";
            case "ㅂ", "ㅍ" -> "ㅂ";
            case "ㅇ" -> "ㅇ";
            default -> "";
        };
    }

    private char compose(String initial, String vowel, String fin) {
        int initialIndex = indexOf(INITIALS, initial);
        int vowelIndex = indexOf(VOWELS, vowel);
        int finalIndex = indexOf(FINALS, fin);
        return (char) (HANGUL_BASE + ((initialIndex * VOWEL_COUNT) + vowelIndex) * FINAL_COUNT + finalIndex);
    }

    private int indexOf(String[] values, String target) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(target)) {
                return i;
            }
        }
        return 0;
    }

    private record Initial(String value, int nextIndex) {
    }

    private record Vowel(String value, int nextIndex) {
    }

    private record Segment(String initial, String vowel, String fin, int nextIndex) {
    }

    public record ConversionResult(String koreanName, NameConversionSource source) {
    }
}

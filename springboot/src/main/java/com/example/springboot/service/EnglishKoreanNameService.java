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

    private static final Map<String, String> PRIORITY_NAME_MAP = new LinkedHashMap<>();

    static {
        // --- Traditional Priority Mappings ---
        PRIORITY_NAME_MAP.put("ANITA", "\uC544\uB2C8\uD0C0");
        PRIORITY_NAME_MAP.put("ANDAYANI", "\uC548\uB2E4\uC57C\uB2C8");
        PRIORITY_NAME_MAP.put("LUKFINTIA", "\uB8E8\uD06C\uD540\uD2F0\uC544");
        PRIORITY_NAME_MAP.put("FILIA", "\uD544\uB9AC\uC544");
        PRIORITY_NAME_MAP.put("RAHMAT", "\uB77C\uD750\uB9DB");
        PRIORITY_NAME_MAP.put("MUTAZIM", "\uBB34\uD0C0\uC9D0");
        PRIORITY_NAME_MAP.put("EKA", "\uC5D0\uCE74");
        PRIORITY_NAME_MAP.put("PUTRA", "\uD478\uD2B8\uB77C");
        PRIORITY_NAME_MAP.put("SILVANA", "\uC2E4\uBC14\uB098");
        PRIORITY_NAME_MAP.put("RESKI", "\uB808\uC2A4\uD0A4");
        PRIORITY_NAME_MAP.put("MULIAWAN", "\uBB3C\uB9AC\uC544\uC644");
        PRIORITY_NAME_MAP.put("MARWIYA", "\uB9C8\uB974\uC704\uC57C");
        PRIORITY_NAME_MAP.put("ARIFUDDIN", "\uC544\uB9AC\uD478\uB518");
        PRIORITY_NAME_MAP.put("TIMBANG", "\uD300\uBC29");
        PRIORITY_NAME_MAP.put("MAYRIDA", "\uB9C8\uC774\uB9AC\uB2E4");
        PRIORITY_NAME_MAP.put("NUDE", "\uB204\uB370");
        PRIORITY_NAME_MAP.put("HAMMA", "\uD568\uB9C8");
        PRIORITY_NAME_MAP.put("SUCI", "\uC218\uCE58");
        PRIORITY_NAME_MAP.put("PRATIWI", "\uD504\uB77C\uD2F0\uC704");
        PRIORITY_NAME_MAP.put("MANGANTAR", "\uB9DD\uC548\uD0C0\uB974");
        PRIORITY_NAME_MAP.put("EVI", "\uC5D0\uBE44");
        PRIORITY_NAME_MAP.put("SULTRIANA", "\uC220\uD2B8\uB9AC\uC544\uB098");
        PRIORITY_NAME_MAP.put("ZACHRI", "\uC790\uD06C\uB9AC");
        PRIORITY_NAME_MAP.put("LENDA", "\uB80C\uB2E4");
        PRIORITY_NAME_MAP.put("LIEM", "\uB9AC\uC5E0");
        PRIORITY_NAME_MAP.put("AULIA", "\uC544\uC6B8\uB9AC\uC544");
        PRIORITY_NAME_MAP.put("ENJELINA", "\uC5D4\uC824\uB9AC\uB098");
        PRIORITY_NAME_MAP.put("RAUFIKA", "\uB77C\uC6B0\uD53C\uCE74");
        PRIORITY_NAME_MAP.put("SUNARYO", "\uC218\uB098\uB974\uC694");
        PRIORITY_NAME_MAP.put("YUNITA", "\uC720\uB2C8\uD0C0");
        PRIORITY_NAME_MAP.put("MANDRASARI", "\uB9CC\uB4DC\uB77C\uC0AC\uB9AC");
        PRIORITY_NAME_MAP.put("YULIATI", "\uC728\uB9AC\uC544\uD2F0");
        PRIORITY_NAME_MAP.put("NANIK", "\uB098\uB2C9");
        PRIORITY_NAME_MAP.put("HARIANI", "\uD558\uB9AC\uC544\uB2C8");
        PRIORITY_NAME_MAP.put("RIVANA", "\uB9AC\uBC14\uB098");
        PRIORITY_NAME_MAP.put("AMELIA", "\uC544\uBA5C\uB9AC\uC544");
        PRIORITY_NAME_MAP.put("FIONITA", "\uD53C\uC624\uB2C8\uD0C0");
        PRIORITY_NAME_MAP.put("HADI", "\uD558\uB514");
        PRIORITY_NAME_MAP.put("HANDRIAN", "\uD55C\uB4DC\uB9AC\uC548");
        PRIORITY_NAME_MAP.put("KANG", "\uAC15");
        PRIORITY_NAME_MAP.put("BONGJU", "\uBD09\uC8FC");

        // --- Comprehensive Chinese Pinyin Syllables (Standard Korean Pronunciation) ---
        String[][] pinyinData = {
            {"A", "\uC544"}, {"AI", "\uC560"}, {"AN", "\uC548"}, {"ANG", "\uC559"}, {"AO", "\uC624"},
            {"BA", "\uD30C"}, {"BAI", "\uBC31"}, {"BAN", "\uBC18"}, {"BANG", "\uBC29"}, {"BAO", "\uBC34"},
            {"BEI", "\uBC30"}, {"BEN", "\uBC22"}, {"BENG", "\uBD95"}, {"BI", "\uBE44"}, {"BIAN", "\uBCC0"},
            {"BIAO", "\uD45C"}, {"BIE", "\uBCC4"}, {"BIN", "\uBE48"}, {"BING", "\uBCD1"}, {"BO", "\uBC15"}, {"BU", "\uBD80"},
            {"CAI", "\uCC44"}, {"CAN", "\uCC2C"}, {"CANG", "\uCC3D"}, {"CAO", "\uC870"}, {"CE", "\uCC45"}, {"CEN", "\uC7A0"},
            {"CHA", "\uCC28"}, {"CHAI", "\uCC44"}, {"CHAN", "\uCC2C"}, {"CHANG", "\uC7A5"}, {"CHAO", "\uCD08"},
            {"CHE", "\uAC70"}, {"CHEN", "\uC9C4"}, {"CHENG", "\uC131"}, {"CHI", "\uC9C0"}, {"CHONG", "\uCDA9"},
            {"CHOU", "\uC8FC"}, {"CHU", "\uCD94"}, {"CHUAN", "\uCC1C"}, {"CHUANG", "\uCC3D"}, {"CHUI", "\uCD94"},
            {"CHUN", "\uCD98"}, {"CHUO", "\uD0 탁"}, {"CI", "\uC790"}, {"CONG", "\uC885"}, {"COU", "\uC8FC"},
            {"CU", "\uCD08"}, {"CUAN", "\uCC2C"}, {"CUI", "\uCD5C"}, {"CUN", "\uCD0C"}, {"CUO", "\uCC29"},
            {"DA", "\uB300"}, {"DAI", "\uB300"}, {"DAN", "\uB2E8"}, {"DANG", "\uB2F9"}, {"DAO", "\uB304"},
            {"DE", "\uB355"}, {"DENG", "\uB4F1"}, {"DI", "\uC9C0"}, {"DIAN", "\uC804"}, {"DIAO", "\uC870"},
            {"DIE", "\uC811"}, {"DING", "\uC815"}, {"DIU", "\uB450"}, {"DONG", "\uB3D9"}, {"DOU", "\uB450"},
            {"DU", "\uB450"}, {"DUAN", "\uB2E8"}, {"DUI", "\uB300"}, {"DUN", "\uB3C8"}, {"DUO", "\uB2E4"},
            {"E", "\uC544"}, {"EN", "\uC540"}, {"ER", "\uC774"}, {"FA", "\uBC95"}, {"FAN", "\uBC94"}, {"FANG", "\uBC29"},
            {"FEI", "\uBE44"}, {"FEN", "\uBD84"}, {"FENG", "\uD48D"}, {"FO", "\uBD88"}, {"FOU", "\uBD80"}, {"FU", "\uBD80"},
            {"GA", "\uAC00"}, {"GAI", "\uAC1C"}, {"GAN", "\uAC04"}, {"GANG", "\uAC15"}, {"GAO", "\uACE0"},
            {"GE", "\uAC00"}, {"GEN", "\uADFC"}, {"GENG", "\uACBD"}, {"GONG", "\uAC19"}, {"GOU", "\uAD6C"},
            {"GU", "\uACE0"}, {"GUA", "\uACFC"}, {"GUAI", "\uAC34"}, {"GUAN", "\uAD00"}, {"GUANG", "\uAD11"},
            {"GUI", "\uADC0"}, {"GUN", "\uACE4"}, {"GUO", "\uAD6D"}, {"HA", "\uD558"}, {"HAI", "\uD574"},
            {"HAN", "\uD55C"}, {"HANG", "\uD56D"}, {"HAO", "\uD638"}, {"HE", "\uD558"}, {"HEI", "\uD751"},
            {"HEN", "\uD764"}, {"HENG", "\uD615"}, {"HONG", "\uD64D"}, {"HOU", "\uD6C4"}, {"HU", "\uD638"},
            {"HUA", "\uD654"}, {"HUAI", "\uD68C"}, {"HUAN", "\uD658"}, {"HUANG", "\uD669"}, {"HUI", "\uD718"},
            {"HUN", "\uD63C"}, {"HUO", "\uD654"}, {"JI", "\uC9C0"}, {"JIA", "\uAC00"}, {"JIAN", "\uAC74"},
            {"JIANG", "\uAC15"}, {"JIAO", "\uAC50"}, {"JIE", "\uACC4"}, {"JIN", "\uC9C4"}, {"JING", "\uACBD"},
            {"JIONG", "\uACBD"}, {"JIU", "\uAD6C"}, {"JU", "\uAC70"}, {"JUAN", "\uAD0C"}, {"JUE", "\uAC01"},
            {"JUN", "\uADE0"}, {"KA", "\uAC00"}, {"KAI", "\uAC1C"}, {"KAN", "\uAC04"}, {"KANG", "\uAC15"},
            {"KE", "\uACFC"}, {"KEN", "\uAC04"}, {"KENG", "\uAC31"}, {"KONG", "\uAC19"}, {"KOU", "\uAD6C"},
            {"KU", "\uACE0"}, {"KUA", "\uACFC"}, {"KUAI", "\uD0DC"}, {"KUAN", "\uAD00"}, {"KUANG", "\uAD11"},
            {"KUI", "\uADDC"}, {"KUN", "\uACE4"}, {"KUO", "\uAD04"}, {"LA", "\uB77C"}, {"LAI", "\uB798"},
            {"LAN", "\uB780"}, {"LANG", "\uB791"}, {"LAO", "\uB85C"}, {"LE", "\uB77D"}, {"LEI", "\uB8B0"},
            {"LENG", "\uB7AD"}, {"LI", "\uB9AC"}, {"LIA", "\uB7B4"}, {"LIAN", "\uB828"}, {"LIANG", "\uB7C9"},
            {"LIAO", "\uB8CC"}, {"LIE", "\uB82C"}, {"LIN", "\uB9BC"}, {"LING", "\uB839"}, {"LIU", "\uB958"},
            {"LONG", "\uB8E1"}, {"LOU", "\uB8E8"}, {"LU", "\uB85C"}, {"LUAN", "\uB780"}, {"LUN", "\uB85C"},
            {"LUO", "\uB77C"}, {"MA", "\uB9C8"}, {"MAI", "\uB9E4"}, {"MAN", "\uB9CC"}, {"MANG", "\uB9DD"},
            {"MAO", "\uBAA8"}, {"MEI", "\uBBF8"}, {"MEN", "\uBB38"}, {"MENG", "\uB9F9"}, {"MI", "\uBBF8"},
            {"MIAN", "\uBA74"}, {"MIAO", "\uBA98"}, {"MIE", "\uBA78"}, {"MIN", "\uBBFC"}, {"MING", "\uBA85"},
            {"MIU", "\uBB34"}, {"MO", "\uBAA8"}, {"MOU", "\uBAA8"}, {"MU", "\uBAA9"}, {"NA", "\u004E\u0041"},
            {"NAI", "\uB0B4"}, {"NAN", "\uB0A8"}, {"NANG", "\uB0AD"}, {"NAO", "\uB1CC"}, {"NEI", "\uB0B4"},
            {"NEN", "\uB208"}, {"NENG", "\uB211"}, {"NI", "\uB2C8"}, {"NIAN", "\uB144"}, {"NIANG", "\uB0AD"},
            {"NIAO", "\uC870"}, {"NIE", "\uC12D"}, {"NIN", "\uB2D8"}, {"NING", "\uB155"}, {"NIU", "\uC6B0"},
            {"NONG", "\uB18D"}, {"NOU", "\uB204"}, {"NU", "\uB178"}, {"NUAN", "\uB09C"}, {"NUO", "\uB099"},
            {"OU", "\uAD6C"}, {"PA", "\uD30C"}, {"PAI", "\uD30C"}, {"PAN", "\uD310"}, {"PANG", "\uBC29"},
            {"PAO", "\uD3EC"}, {"PEI", "\uBC30"}, {"PEN", "\uBD84"}, {"PENG", "\uD33D"}, {"PI", "\uD33C"},
            {"PIAN", "\uD3B8"}, {"PIAO", "\uD45C"}, {"PIE", "\uBCC4"}, {"PIN", "\uBE48"}, {"PING", "\uD3C9"},
            {"PO", "\uD30C"}, {"POU", "\uBD80"}, {"PU", "\uD3EC"}, {"QI", "\uAE30"}, {"QIA", "\uAC00"},
            {"QIAN", "\uCC1C"}, {"QIANG", "\uAC15"}, {"QIAO", "\uAC50"}, {"QIE", "\uC808"}, {"QIN", "\uC9C4"},
            {"QING", "\uCCAD"}, {"QIONG", "\uAD81"}, {"QIU", "\uAD6C"}, {"QU", "\uAD6C"}, {"QUAN", "\uAD0C"},
            {"QUE", "\uAC01"}, {"QUN", "\uADE0"}, {"RAN", "\uC5FC"}, {"RANG", "\uC591"}, {"RAO", "\uC694"},
            {"RE", "\uC5F4"}, {"REN", "\uC778"}, {"RENG", "\uC789"}, {"RI", "\uC77C"}, {"RONG", "\uC735"},
            {"ROU", "\uC720"}, {"RU", "\uC5EC"}, {"RUAN", "\uC5F0"}, {"RUI", "\uC608"}, {"RUN", "\uC724"},
            {"RUO", "\uC57D"}, {"SA", "\uC0B4"}, {"SAI", "\uC0C8"}, {"SAN", "\uC0B0"}, {"SANG", "\uC0C1"},
            {"SAO", "\uC18C"}, {"SE", "\uC0C9"}, {"SEN", "\uC0BC"}, {"SENG", "\uC2B9"}, {"SHA", "\uC0AC"},
            {"SHAI", "\uC1C4"}, {"SHAN", "\uC0B0"}, {"SHANG", "\uC0C1"}, {"SHAO", "\uC18C"}, {"SHE", "\uC0AC"},
            {"SHEN", "\uC2E0"}, {"SHENG", "\uC131"}, {"SHI", "\uC11D"}, {"SHOU", "\uC218"}, {"SHU", "\uC11C"},
            {"SHUA", "\uC1C4"}, {"SHUAI", "\uC1C4"}, {"SHUAN", "\uC120"}, {"SHUANG", "\uC0C1"}, {"SHUI", "\uC218"},
            {"SHUN", "\uC21C"}, {"SHUO", "\uC124"}, {"SI", "\uC0AC"}, {"SONG", "\uC1A1"}, {"SOU", "\uC218"},
            {"SU", "\uC18C"}, {"SUAN", "\uC0B0"}, {"SUI", "\uC218"}, {"SUN", "\uC190"}, {"SUO", "\uC18C"},
            {"TA", "\uD0C0"}, {"TAI", "\uD0DC"}, {"TAN", "\uD0C4"}, {"TANG", "\uB2F9"}, {"TAO", "\uB3C4"},
            {"TE", "\uD2B9"}, {"TENG", "\uB4F1"}, {"TI", "\uC81C"}, {"TIAN", "\uCC1C"}, {"TIAO", "\uC870"},
            {"TIE", "\uCCA0"}, {"TING", "\uC815"}, {"TONG", "\uB3D9"}, {"TOU", "\uD22C"}, {"TU", "\uD1A0"},
            {"TUAN", "\uB2E8"}, {"TUI", "\uD1F4"}, {"TUN", "\uB3C8"}, {"TUO", "\uD0C0"}, {"WA", "\uC640"},
            {"WAI", "\uC678"}, {"WAN", "\uC644"}, {"WANG", "\uC651"}, {"WEI", "\uC704"}, {"WEN", "\uBB38"},
            {"WENG", "\uC639"}, {"WO", "\uC544"}, {"WU", "\uC624"}, {"XI", "\uC2DC"}, {"XIA", "\uD558"},
            {"XIAN", "\uC120"}, {"XIANG", "\uC0C1"}, {"XIAO", "\uC18C"}, {"XIE", "\uC0AC"}, {"XIN", "\uC2E0"},
            {"XING", "\uC131"}, {"XIONG", "\uC6C5"}, {"XIU", "\uC218"}, {"XU", "\uC11C"}, {"XUAN", "\uC120"},
            {"XUE", "\uC124"}, {"XUN", "\uC21C"}, {"YA", "\uC544"}, {"YAN", "\uC5F0"}, {"YANG", "\uC591"},
            {"YAO", "\uC694"}, {"YE", "\uC608"}, {"YI", "\uC774"}, {"YIN", "\uC778"}, {"YING", "\uC601"},
            {"YO", "\uC694"}, {"YONG", "\uC6A9"}, {"YOU", "\uC720"}, {"YU", "\uC6B0"}, {"YUAN", "\uC6D0"},
            {"YUE", "\uC6D4"}, {"YUN", "\uC724"}, {"ZA", "\uC7A1"}, {"ZAI", "\uC7AC"}, {"ZAN", "\uCC2C"},
            {"ZANG", "\uC7A5"}, {"ZAO", "\uC870"}, {"ZE", "\uD0DD"}, {"ZEI", "\uC801"}, {"ZEN", "\uC998"},
            {"ZENG", "\uC99D"}, {"ZHA", "\uC0AC"}, {"ZHAI", "\uD0DD"}, {"ZHAN", "\uC804"}, {"ZHANG", "\uC7A5"},
            {"ZHAO", "\uC870"}, {"ZHE", "\uC808"}, {"ZHEN", "\uC9C4"}, {"ZHENG", "\uC815"}, {"ZHI", "\uC9C0"},
            {"ZHONG", "\uC911"}, {"ZHOU", "\uC8FC"}, {"ZHU", "\uC8FC"}, {"ZHUA", "\uC870"}, {"ZHUAI", "\uD60C"},
            {"ZHUAN", "\uC804"}, {"ZHUANG", "\uC7A5"}, {"ZHUI", "\uCD94"}, {"ZHUN", "\uC900"}, {"ZHUO", "\uD0C1"},
            {"ZI", "\uC790"}, {"ZONG", "\uC885"}, {"ZOU", "\uC8FC"}, {"ZU", "\uC871"}, {"ZUAN", "\uCC2C"},
            {"ZUI", "\uCD5C"}, {"ZUN", "\uC874"}, {"ZUO", "\uC8CC"}
        };

        for (String[] entry : pinyinData) {
            PRIORITY_NAME_MAP.put(entry[0], entry[1]);
        }
    }

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
        if (language != NameLanguage.EN && language != NameLanguage.ZH) {
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

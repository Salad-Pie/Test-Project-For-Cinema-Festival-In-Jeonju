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
            {"A", "\uC544"}, {"AI", "\uC544\uC774"}, {"AN", "\uC548"}, {"ANG", "\uC559"}, {"AO", "\uC544\uC624"},
            {"BA", "\uBC14"}, {"BAI", "\uBC14\uC774"}, {"BAN", "\uBC18"}, {"BANG", "\uBC29"}, {"BAO", "\uBC14\uC624"},
            {"BEI", "\uBCA0\uC774"}, {"BEN", "\uBC24"}, {"BENG", "\uBD25"}, {"BI", "\uBE44"}, {"BIAN", "\uBCC0"},
            {"BIAO", "\uBE44\uC544\uC624"}, {"BIE", "\uBE44\uC5D0"}, {"BIN", "\uBE48"}, {"BING", "\uBE59"}, {"BO", "\uBC34"}, {"BU", "\uBD80"},
            {"CAI", "\uCC28\uC774"}, {"CAN", "\uCC2C"}, {"CANG", "\uCC3D"}, {"CAO", "\uCC28\uC624"}, {"CE", "\uCC28"}, {"CEN", "\uCC90"},
            {"CHA", "\uCC28"}, {"CHAI", "\uCC28\uC774"}, {"CHAN", "\uCC2C"}, {"CHANG", "\uCC3D"}, {"CHAO", "\uCC28\uC624"},
            {"CHE", "\uCC28"}, {"CHEN", "\uCC90"}, {"CHENG", "\uCCAD"}, {"CHI", "\uCE58"}, {"CHONG", "\uCDA9"},
            {"CHOU", "\uCD08\uC6B0"}, {"CHU", "\uCD94"}, {"CHUAN", "\uCD3D"}, {"CHUANG", "\uCD3D"}, {"CHUI", "\uCD94\uC774"},
            {"CHUN", "\uCD98"}, {"CHUO", "\uCD14"}, {"CI", "\uCE20"}, {"CONG", "\uCD1D"}, {"COU", "\uCD08\uC6B0"},
            {"CU", "\uCD94"}, {"CUAN", "\uCD3D"}, {"CUI", "\uCD94\uC774"}, {"CUN", "\uCD98"}, {"CUO", "\uCD14"},
            {"DA", "\uB2E4"}, {"DAI", "\uB2E4\uC774"}, {"DAN", "\uB2E8"}, {"DANG", "\uB2F9"}, {"DAO", "\uB2E4\uC624"},
            {"DE", "\uB354"}, {"DENG", "\uB369"}, {"DI", "\uB514"}, {"DIAN", "\uB514\uC548"}, {"DIAO", "\uB2E4\uC624"},
            {"DIE", "\uB514\uC5D0"}, {"DING", "\uB529"}, {"DIU", "\uB4C0"}, {"DONG", "\uB465"}, {"DOU", "\uB354\uC6B0"},
            {"DU", "\uB450"}, {"DUAN", "\uB464"}, {"DUI", "\uB450\uC774"}, {"DUN", "\uB45C"}, {"DUO", "\uB464"},
            {"E", "\uC5B4"}, {"EN", "\uC5B8"}, {"ER", "\uC5BC"}, {"FA", "\uD30C"}, {"FAN", "\uD310"}, {"FANG", "\uD321"},
            {"FEI", "\uD398\uC774"}, {"FEN", "\uD338"}, {"FENG", "\uD37D"}, {"FO", "\uD3EC"}, {"FOU", "\uD3EC\uC6B0"}, {"FU", "\uD478"},
            {"GA", "\uAC00"}, {"GAI", "\uAC00\uC774"}, {"GAN", "\uAC04"}, {"GANG", "\uAC15"}, {"GAO", "\uAC00\uC624"},
            {"GE", "\uAC70"}, {"GEN", "\uAC74"}, {"GENG", "\uAC71"}, {"GONG", "\uACF5"}, {"GOU", "\uAC70\uC6B0"},
            {"GU", "\uAD6C"}, {"GUA", "\uACFC"}, {"GUAI", "\uACFC\uC774"}, {"GUAN", "\uAD00"}, {"GUANG", "\uAD11"},
            {"GUI", "\uAD6C\uC774"}, {"GUN", "\uAD70"}, {"GUO", "\uAD84"}, {"HA", "\uD558"}, {"HAI", "\uD558\uC774"},
            {"HAN", "\uD55C"}, {"HANG", "\uD56D"}, {"HAO", "\uD558\uC624"}, {"HE", "\uD558"}, {"HEI", "\uD5E4\uC774"},
            {"HEN", "\uD5CC"}, {"HENG", "\uD5E5"}, {"HONG", "\uD64D"}, {"HOU", "\uD6C4"}, {"HU", "\uD638"},
            {"HUA", "\uD654"}, {"HUAI", "\uD68C"}, {"HUAN", "\uD658"}, {"HUANG", "\uD669"}, {"HUI", "\uD618"},
            {"HUN", "\uD63C"}, {"HUO", "\uD654"}, {"JI", "\uC9C0"}, {"JIA", "\uC9C0\uC544"}, {"JIAN", "\uC9C0\uC548"},
            {"JIANG", "\uC9C0\uC559"}, {"JIAO", "\uC9C0\uC544\uC624"}, {"JIE", "\uC9C0\uC5D0"}, {"JIN", "\uC9C4"}, {"JING", "\uC9D5"},
            {"JIONG", "\uC911"}, {"JIU", "\uC9C0\uC6B0"}, {"JU", "\uC96C"}, {"JUAN", "\uC918"}, {"JUE", "\uC910"},
            {"JUN", "\uC900"}, {"KA", "\uCE74"}, {"KAI", "\uCE74\uC774"}, {"KAN", "\uCE78"}, {"KANG", "\uCE89"},
            {"KE", "\uCEE4"}, {"KEN", "\uCEE8"}, {"KENG", "\uCEE1"}, {"KONG", "\uCF69"}, {"KOU", "\uCEE4\uC6B0"},
            {"KU", "\uD06C"}, {"KUA", "\uD640"}, {"KUAI", "\uD0DC"}, {"KUAN", "\uAD00"}, {"KUANG", "\uAD11"},
            {"KUI", "\uADDC"}, {"KUN", "\uACE4"}, {"KUO", "\uAD04"}, {"LA", "\uB77C"}, {"LAI", "\uB798"},
            {"LAN", "\uB780"}, {"LANG", "\uB791"}, {"LAO", "\uB77C\uC624"}, {"LE", "\uB7EC"}, {"LEI", "\uB808\uC774"},
            {"LENG", "\uB7EC\uC53D"}, {"LI", "\uB9AC"}, {"LIA", "\uB7B4"}, {"LIAN", "\uB828"}, {"LIANG", "\uB7C9"},
            {"LIAO", "\uB8CC"}, {"LIE", "\uB82C"}, {"LIN", "\uB9BC"}, {"LING", "\uB839"}, {"LIU", "\uB958"},
            {"LONG", "\uB8E1"}, {"LOU", "\uB8E8"}, {"LU", "\uB85C"}, {"LUAN", "\uB780"}, {"LUN", "\uB85C"},
            {"LUO", "\uB77C"}, {"MA", "\uB9C8"}, {"MAI", "\uB9E4"}, {"MAN", "\uB9CC"}, {"MANG", "\uB9DD"},
            {"MAO", "\uBAA8"}, {"MEI", "\uBBF8"}, {"MEN", "\uBB38"}, {"MENG", "\uBA4D"}, {"MI", "\uBBF8"},
            {"MIAN", "\uBA74"}, {"MIAO", "\uBA98"}, {"MIE", "\uBA78"}, {"MIN", "\uBBFC"}, {"MING", "\uBA85"},
            {"MIU", "\uBB34"}, {"MO", "\uBAA8"}, {"MOU", "\uBAA8"}, {"MU", "\uBAA9"}, {"NA", "\uB098"},
            {"NAI", "\uB0B4"}, {"NAN", "\uB0A8"}, {"NANG", "\uB0AD"}, {"NAO", "\uB098\uC624"}, {"NEI", "\uB124\uC774"},
            {"NEN", "\uB10C"}, {"NENG", "\uB108\uC53D"}, {"NI", "\uB2C8"}, {"NIAN", "\uB2CC"}, {"NIANG", "\uB0AD"},
            {"NIAO", "\uB098\uC624"}, {"NIE", "\uB2CC"}, {"NIN", "\uB2CC"}, {"NING", "\uB2CC"}, {"NIU", "\uB274"},
            {"NONG", "\uB18D"}, {"NOU", "\uB178\uC6B0"}, {"NU", "\uB204"}, {"NUAN", "\uB09C"}, {"NUO", "\uB108"},
            {"OU", "\uC624\uC6B0"}, {"PA", "\uD30C"}, {"PAI", "\uD30C\uC774"}, {"PAN", "\uD310"}, {"PANG", "\uBC29"},
            {"PAO", "\uD30C\uC624"}, {"PEI", "\uD398\uC774"}, {"PEN", "\uD338"}, {"PENG", "\uD37D"}, {"PI", "\uD33C"},
            {"PIAN", "\uD3B8"}, {"PIAO", "\uD45C"}, {"PIE", "\uBCC4"}, {"PIN", "\uBE48"}, {"PING", "\uD3C9"},
            {"PO", "\uD30C"}, {"POU", "\uBD80"}, {"PU", "\uD3EC"}, {"QI", "\uCE58"}, {"QIA", "\uCE58\uC544"},
            {"QIAN", "\uCE58\uC548"}, {"QIANG", "\uCE6D"}, {"QIAO", "\uCE58\uC544\uC624"}, {"QIE", "\uCE58\uC5D0"}, {"QIN", "\uCE5C"},
            {"QING", "\uCE6D"}, {"QIONG", "\uCDA9"}, {"QIU", "\uCD94"}, {"QU", "\uCD00"}, {"QUAN", "\uCD00\uC548"},
            {"QUE", "\uCD14"}, {"QUN", "\uCD98"}, {"RAN", "\uB780"}, {"RANG", "\uB791"}, {"RAO", "\uB77C\uC624"},
            {"RE", "\uB7EC"}, {"REN", "\uB720"}, {"RENG", "\uB7EC\uC53D"}, {"RI", "\uB9AC"}, {"RONG", "\uB821"},
            {"ROU", "\uB85C\uC6B0"}, {"RU", "\uB8E8"}, {"RUAN", "\uB834"}, {"RUI", "\uB8E8\uC774"}, {"RUN", "\uB8E1"},
            {"RUO", "\uB800"}, {"SA", "\uC0AC"}, {"SAI", "\uC0AC\uC774"}, {"SAN", "\uC0B0"}, {"SANG", "\uC0C1"},
            {"SAO", "\uC0AC\uC624"}, {"SE", "\uC11C"}, {"SEN", "\uC120"}, {"SENG", "\uC131"}, {"SHA", "\uC0AC"},
            {"SHAI", "\uC0AC\uC774"}, {"SHAN", "\uC0B0"}, {"SHANG", "\uC0C1"}, {"SHAO", "\uC0AC\uC624"}, {"SHE", "\uC11C"},
            {"SHEN", "\uC120"}, {"SHENG", "\uC131"}, {"SHI", "\uC218"}, {"SHOU", "\uC11C\uC6B0"}, {"SHU", "\uC218"},
            {"SHUA", "\uC218\uC544"}, {"SHUAI", "\uC218\uC544\uC774"}, {"SHUAN", "\uC218\uC548"}, {"SHUANG", "\uC218\uC559"}, {"SHUI", "\uC218\uC774"},
            {"SHUN", "\uC21C"}, {"SHUO", "\uC218\uC624"}, {"SI", "\uC218"}, {"SONG", "\uC22D"}, {"SOU", "\uC11C\uC6B0"},
            {"SU", "\uC218"}, {"SUAN", "\uC218\uC548"}, {"SUI", "\uC218\uC774"}, {"SUN", "\uC21C"}, {"SUO", "\uC218\uC624"},
            {"TA", "\uD0C0"}, {"TAI", "\uD0C0\uC774"}, {"TAN", "\uD0C4"}, {"TANG", "\uD0D5"}, {"TAO", "\uD0C0\uC624"},
            {"TE", "\uD130"}, {"TENG", "\uD131"}, {"TI", "\uD0F0"}, {"TIAN", "\uD150"}, {"TIAO", "\uD2F0\uC544\uC624"},
            {"TIE", "\uD2F0\uC5D0"}, {"TING", "\uD305"}, {"TONG", "\uD1B5"}, {"TOU", "\uD130\uC6B0"}, {"TU", "\uD22C"},
            {"TUAN", "\uD22C\uC548"}, {"TUI", "\uD22C\uC774"}, {"TUN", "\uD230"}, {"TUO", "\uD22C\uC624"}, {"WA", "\uC640"},
            {"WAI", "\uC640\uC774"}, {"WAN", "\uC644"}, {"WANG", "\uC655"}, {"WEI", "\uC6E8\uC774"}, {"WEN", "\uC6D0"},
            {"WENG", "\uC6E1"}, {"WO", "\uC6CC"}, {"WU", "\uC6B0"}, {"XI", "\uC2DC"}, {"XIA", "\uC2DC\uC544"},
            {"XIAN", "\uC2DC\uC548"}, {"XIANG", "\uC2DC\uC559"}, {"XIAO", "\uC2DC\uC544\uC624"}, {"XIE", "\uC2DC\uC5D0"}, {"XIN", "\uC2E0"},
            {"XING", "\uC2F1"}, {"XIONG", "\uC211"}, {"XIU", "\uC210"}, {"XU", "\uC26C"}, {"XUAN", "\uC2DC\uC548"},
            {"XUE", "\uC2DC\uC5D0"}, {"XUN", "\uC2E0"}, {"YA", "\uC57C"}, {"YAN", "\uC580"}, {"YANG", "\uC591"},
            {"YAO", "\uC57C\uC624"}, {"YE", "\uC608"}, {"YI", "\uC774"}, {"YIN", "\uC778"}, {"YING", "\uC789"},
            {"YO", "\uC694"}, {"YONG", "\uC735"}, {"YOU", "\uC720"}, {"YU", "\uC704"}, {"YUAN", "\uC704\uC548"},
            {"YUE", "\uC608"}, {"YUN", "\uC708"}, {"ZA", "\uC790"}, {"ZAI", "\uC790\uC774"}, {"ZAN", "\uC794"},
            {"ZANG", "\uC7A5"}, {"ZAO", "\uC790\uC624"}, {"ZE", "\uC800"}, {"ZEI", "\uC800\uC774"}, {"ZEN", "\uC804"},
            {"ZENG", "\uC815"}, {"ZHA", "\uC790"}, {"ZHAI", "\uC790\uC774"}, {"ZHAN", "\uC794"}, {"ZHANG", "\uC7A5"},
            {"ZHAO", "\uC790\uC624"}, {"ZHE", "\uC800"}, {"ZHEN", "\uC804"}, {"ZHENG", "\uC815"}, {"ZHI", "\uC913"},
            {"ZHONG", "\uC911"}, {"ZHOU", "\uC800\uC6B0"}, {"ZHU", "\uC8FC"}, {"ZHUA", "\uC8FC\uC544"}, {"ZHUAI", "\uC8FC\uC544\uC774"},
            {"ZHUAN", "\uC8FC\uC548"}, {"ZHUANG", "\uC8FC\uC559"}, {"ZHUI", "\uC8FC\uC774"}, {"ZHUN", "\uC900"}, {"ZHUO", "\uC8FC\uC624"},
            {"ZI", "\uC988"}, {"ZONG", "\uC885"}, {"ZOU", "\uC81C\uC6B0"}, {"ZU", "\uC8FC"}, {"ZUAN", "\uC8FC\uC548"},
            {"ZUI", "\uC8FC\uC774"}, {"ZUN", "\uC900"}, {"ZUO", "\uC8FC\uC624"}
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

import com.example.springboot.service.EnglishKoreanNameService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NameConversionBatchExporter {
    public static void main(String[] args) throws Exception {
        EnglishKoreanNameService service = new EnglishKoreanNameService();
        Map<String, String> priorityMap = service.getPriorityNameMap();
        List<String> names = buildNames(priorityMap);
        List<String> lines = new ArrayList<>();
        lines.add("index,englishName,koreanName,source,mapHit");

        int index = 1;
        for (String name : names) {
            EnglishKoreanNameService.ConversionResult result = service.convertByOfficialLoanwordPolicy(name);
            boolean mapHit = priorityMap.containsKey(name);
            lines.add(index + "," + csv(name) + "," + csv(result.koreanName()) + "," + csv(String.valueOf(result.source())) + "," + mapHit);
            index++;
        }

        Path output = Path.of(args[0]);
        Files.write(output, lines, StandardCharsets.UTF_8);
    }

    private static List<String> buildNames(Map<String, String> priorityMap) {
        Set<String> names = new LinkedHashSet<>(priorityMap.keySet());
        String[] firstNames = {
                "ALEXANDER", "MICHAEL", "DANIEL", "WILLIAM", "JAMES", "THOMAS", "CHRISTOPHER", "GEORGE", "BENJAMIN", "JACOB",
                "OLIVIA", "EMMA", "SOPHIA", "ISABELLA", "EMILY", "SARAH", "GRACE", "ANNA", "LAURA", "AMELIA"
        };
        String[] lastNames = {
                "SMITH", "JOHNSON", "BROWN", "TAYLOR", "MARTIN", "CLARKE", "ANDERSON", "THOMPSON", "JACKSON", "WALKER"
        };

        for (String firstName : firstNames) {
            for (String lastName : lastNames) {
                names.add(firstName + " " + lastName);
                if (names.size() == 100) {
                    return new ArrayList<>(names);
                }
            }
        }
        return new ArrayList<>(names);
    }

    private static String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }
}
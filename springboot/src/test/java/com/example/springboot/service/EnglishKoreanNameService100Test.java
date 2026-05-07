package com.example.springboot.service;

import com.example.springboot.domain.NameConversionSource;
import com.example.springboot.domain.NameLanguage;
import org.junit.jupiter.api.Test;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class EnglishKoreanNameService100Test {

    @Test
    public void generate100NamesResult() throws IOException {
        EnglishKoreanNameService service = new EnglishKoreanNameService();
        String[] names = {
            // Priority names
            "ANITA ANDAYANI", "LUKFINTIA FILIA", "RAHMAT MUTAZIM EKA PUTRA", "SILVANA RESKI MULIAWAN",
            "MARWIYA ARIFUDDIN TIMBANG", "MAYRIDA NUDE HAMMA", "SUCI PRATIWI MANGANTAR", "EVI SULTRIANA ZACHRI",
            "LENDA LIEM", "AULIA ENJELINA RAUFIKA SUNARYO", "YUNITA MANDRASARI", "YULIATI", "NANIK HARIANI",
            "RIVANA AMELIA FIONITA", "HADI ANDRIAN", "KANG BONGJU",
            // Dictionary names
            "alexander", "amelia", "andrew", "anna", "anthony", "benjamin", "charles", "christopher",
            "daniel", "danielle", "david", "edward", "elizabeth", "emily", "emma", "ethan",
            "george", "grace", "henry", "isabella", "jacob", "james", "jennifer", "john",
            "johnson", "joseph", "joshua", "kevin", "laura", "liam", "lucas", "martin",
            "mary", "michael", "olivia", "paul", "peter", "richard", "robert", "sarah",
            "smith", "sofia", "sophia", "thomas", "william",
            // Additional random names
            "patricia", "linda", "barbara", "susan", "jessica", "karen", "nancy", "lisa",
            "matthew", "betty", "margaret", "mark", "sandra", "donald", "ashley", "steven",
            "kimberly", "donna", "michelle", "kenneth", "dorothy", "carol", "brian", "amanda",
            "melissa", "deborah", "ronald", "stephanie", "timothy", "rebecca", "jason", "sharon",
            "jeffrey", "ryan", "cynthia", "kathleen", "gary", "amy", "nicholas", "shirley",
            "eric", "angela", "jonathan", "helen", "stephen", "larry", "brenda", "justin",
            "pamela", "scott", "nicole", "brandon", "samantha", "samuel", "katherine", "gregory"
        };

        String outputPath = "C:\\Users\\dldbs\\Downloads\\english_korean_name_test_results.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("Original English Name | Converted Korean Name | Source\n");
            writer.write("----------------------------------------------------------\n");
            for (String name : names) {
                EnglishKoreanNameService.ConversionResult result = service.convertByOfficialLoanwordPolicy(name);
                writer.write(String.format("%-30s | %-20s | %s\n", name, result.koreanName(), result.source()));
            }
        }
        System.out.println("Result written to: " + outputPath);
    }
}

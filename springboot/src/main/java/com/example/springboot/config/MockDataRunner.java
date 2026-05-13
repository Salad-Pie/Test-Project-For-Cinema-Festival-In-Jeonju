package com.example.springboot.config;

import com.example.springboot.domain.*;
import com.example.springboot.repository.IdentifierCodeRepository;
import com.example.springboot.repository.SignatureRepository;
import com.example.springboot.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class MockDataRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final IdentifierCodeRepository identifierCodeRepository;
    private final SignatureRepository signatureRepository;

    public MockDataRunner(
            UserRepository userRepository,
            IdentifierCodeRepository identifierCodeRepository,
            SignatureRepository signatureRepository
    ) {
        this.userRepository = userRepository;
        this.identifierCodeRepository = identifierCodeRepository;
        this.signatureRepository = signatureRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 5) {
            return; // Already has data
        }

        String[] chineseNames = {"Zhang Wei", "Li Na", "Wang Yang", "Liu Fang", "Chen Ming", "Yang Jie", "Huang Yan", "Zhao Jun", "Wu Min", "Zhou Lei"};
        String[] koreanNames = {"장위", "이나", "왕양", "유방", "진명", "양지에", "황연", "조준", "오민", "주레이"};

        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setNickname("User" + (i + 1));
            user.setFullName(chineseNames[i]);
            user.setEmail("user" + (i + 1) + "@example.com");
            userRepository.save(user);

            IdentifierCode code = new IdentifierCode();
            code.setUser(user);
            code.setCode(String.format("%06d", random.nextInt(1000000)));
            identifierCodeRepository.save(code);

            Signature signature = new Signature();
            signature.setUser(user);
            signature.setOriginalName(chineseNames[i]);
            signature.setKoreanName(koreanNames[i]);
            signature.setRecognizedText(chineseNames[i]);
            signature.setOcrConfidence(0.95);
            
            // 필수 필드 채우기
            signature.setOriginalS3Key("mock/key/" + i);
            signature.setOriginalFileSize(1024L);
            signature.setOriginalContentType("image/png");
            signature.setOcrProvider(OcrProvider.GOOGLE_VISION);
            signature.setOcrStatus(OcrStatus.SUCCESS);
            signature.setDetectedLanguage(SignatureLanguage.EN);
            
            // createdAt은 @PrePersist에서 자동으로 설정됨
            signatureRepository.save(signature);
        }

        System.out.println(">>> 10 Chinese Tourist Mock Data Seeded successfully.");
    }
}

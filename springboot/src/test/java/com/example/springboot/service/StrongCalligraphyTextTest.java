package com.example.springboot.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class StrongCalligraphyTextTest {

    @Test
    void renderStrongCalligraphyTextSample() throws Exception {
        SignatureImageService signatureImageService = new SignatureImageService();

        byte[] png = signatureImageService.renderStrongCalligraphyText("이창섭", 1400, 520);

        Path outputDirectory = Path.of("build", "generated-samples");
        Files.createDirectories(outputDirectory);
        Path output = outputDirectory.resolve("strong-calligraphy-이창섭.png");
        Files.write(output, png);

        assertThat(png).startsWith(new byte[]{(byte) 0x89, 'P', 'N', 'G'});
        assertThat(Files.size(output)).isGreaterThan(0);
    }
}

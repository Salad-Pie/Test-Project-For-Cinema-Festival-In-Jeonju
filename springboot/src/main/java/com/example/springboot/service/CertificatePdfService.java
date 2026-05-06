package com.example.springboot.service;

import com.example.springboot.exception.BusinessException;
import com.example.springboot.exception.ErrorCode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

@Service
public class CertificatePdfService {

    private static final String TEMPLATE_PATH = "/certificates/Certification_Sample.pdf";
    private static final Box ENGLISH_NAME_BOX = new Box(185, 563, 235, 46);
    private static final Box KOREAN_NAME_BOX = new Box(205, 513, 195, 46);
    private static final Box ORIGINAL_SIGNATURE_BOX = new Box(220, 405, 165, 76);
    private static final int IMAGE_SCALE = 4;

    private final SignatureImageService signatureImageService;

    public CertificatePdfService(SignatureImageService signatureImageService) {
        this.signatureImageService = signatureImageService;
    }

    public byte[] renderKoreanCalligraphyCertificate(
            String englishName,
            String koreanName,
            byte[] originalSignaturePng
    ) {
        byte[] templateBytes = loadTemplateBytes();
        try (PDDocument document = Loader.loadPDF(templateBytes);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = document.getPage(0);
            byte[] englishNameImage = signatureImageService.renderCertificateTextBox(
                    englishName,
                    "Georgia",
                    ENGLISH_NAME_BOX.pixelWidth(),
                    ENGLISH_NAME_BOX.pixelHeight(),
                    56,
                    50,
                    true
            );
            byte[] koreanNameImage = signatureImageService.renderCertificateTextBox(
                    koreanName,
                    "궁서",
                    KOREAN_NAME_BOX.pixelWidth(),
                    KOREAN_NAME_BOX.pixelHeight(),
                    64,
                    58,
                    false
            );
            byte[] signatureImage = originalSignaturePng == null || originalSignaturePng.length == 0
                    ? signatureImageService.renderSampleOriginalSignature(ORIGINAL_SIGNATURE_BOX.pixelWidth(), ORIGINAL_SIGNATURE_BOX.pixelHeight())
                    : originalSignaturePng;

            PDImageXObject englishNameXObject = PDImageXObject.createFromByteArray(document, englishNameImage, "english-name");
            PDImageXObject koreanNameXObject = PDImageXObject.createFromByteArray(document, koreanNameImage, "korean-name");
            PDImageXObject signatureXObject = PDImageXObject.createFromByteArray(document, signatureImage, "original-signature");

            try (PDPageContentStream contentStream = new PDPageContentStream(
                    document,
                    page,
                    PDPageContentStream.AppendMode.APPEND,
                    true,
                    true
            )) {
                drawImageFit(contentStream, englishNameXObject, ENGLISH_NAME_BOX);
                drawImageFit(contentStream, koreanNameXObject, KOREAN_NAME_BOX);
                drawImageFit(contentStream, signatureXObject, ORIGINAL_SIGNATURE_BOX);
            }

            document.save(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("failed to render certificate pdf.", e);
        }
    }

    private byte[] loadTemplateBytes() {
        try (InputStream inputStream = CertificatePdfService.class.getResourceAsStream(TEMPLATE_PATH)) {
            if (inputStream == null) {
                throw new BusinessException(ErrorCode.CERTIFICATE_TEMPLATE_NOT_FOUND);
            }
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("failed to load certificate template.", e);
        }
    }

    private void drawImageFit(PDPageContentStream contentStream, PDImageXObject image, Box box) throws IOException {
        float imageRatio = (float) image.getWidth() / image.getHeight();
        float boxRatio = box.width() / box.height();
        float drawWidth = box.width();
        float drawHeight = box.height();
        if (imageRatio > boxRatio) {
            drawHeight = drawWidth / imageRatio;
        } else {
            drawWidth = drawHeight * imageRatio;
        }
        float x = box.x() + (box.width() - drawWidth) / 2;
        float y = box.y() + (box.height() - drawHeight) / 2;
        contentStream.drawImage(image, x, y, drawWidth, drawHeight);
    }

    private record Box(float x, float y, float width, float height) {
        int pixelWidth() {
            return Math.round(width * IMAGE_SCALE);
        }

        int pixelHeight() {
            return Math.round(height * IMAGE_SCALE);
        }
    }
}

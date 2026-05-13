package com.example.springboot.service;

import com.example.springboot.domain.IdeaContest;
import com.example.springboot.domain.MemoImage;
import com.example.springboot.domain.User;
import com.example.springboot.dto.IdeaContestResponse;
import com.example.springboot.dto.MemoImageResponse;
import com.example.springboot.repository.IdeaContestRepository;
import com.example.springboot.repository.UserRepository;
import com.example.springboot.security.JwtTokenProvider;
import com.example.springboot.security.TokenType;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class IdeaContestService {

    private final IdeaContestRepository ideaContestRepository;
    private final UserRepository userRepository;
    private final S3UploadService s3UploadService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PointRewardService pointRewardService;

    public IdeaContestService(
            IdeaContestRepository ideaContestRepository,
            UserRepository userRepository,
            S3UploadService s3UploadService,
            JwtTokenProvider jwtTokenProvider,
            PointRewardService pointRewardService
    ) {
        this.ideaContestRepository = ideaContestRepository;
        this.userRepository = userRepository;
        this.s3UploadService = s3UploadService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.pointRewardService = pointRewardService;
    }

    private static final Logger log = LoggerFactory.getLogger(IdeaContestService.class);

    public IdeaContestResponse create(String token, List<MultipartFile> images) {
        log.info("Idea contest proposal request received. imageCount={}", images == null ? 0 : images.size());
        
        if (token == null || token.isBlank()) {
            log.warn("Idea contest failed: Authorization token is missing.");
            throw new IllegalArgumentException("authorization token is required.");
        }
        if (images == null || images.stream().noneMatch(f -> f != null && !f.isEmpty())) {
            log.warn("Idea contest failed: No valid images provided.");
            throw new IllegalArgumentException("at least one image is required.");
        }

        TokenType tokenType = jwtTokenProvider.extractTokenType(token);
        if (tokenType == TokenType.OAUTH_STATE) {
            log.warn("Idea contest failed: Temporary OAuth state token cannot be used for submission.");
            throw new IllegalArgumentException("login is required for idea contest.");
        }

        Long userId = jwtTokenProvider.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Idea contest failed: User not found. userId={}", userId);
                    return new IllegalArgumentException("user not found.");
                });

        log.info("Creating idea contest for userId={}", userId);
        IdeaContest ideaContest = new IdeaContest();
        ideaContest.setUser(user);

        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                continue;
            }
            S3UploadService.UploadedImage uploadedImage = s3UploadService.uploadMemoImage(image);

            MemoImage memoImage = new MemoImage();
            memoImage.setOriginalFilename(image.getOriginalFilename() == null ? "unknown" : image.getOriginalFilename());
            memoImage.setS3Key(uploadedImage.s3Key());
            memoImage.setFileSize(uploadedImage.fileSize());
            ideaContest.addMemoImage(memoImage);
        }

        IdeaContest saved = ideaContestRepository.save(ideaContest);
        pointRewardService.earnActivityPoints(userId, "아이디어 공모전 제안 완료");
        return toResponse(saved);
    }

    private IdeaContestResponse toResponse(IdeaContest ideaContest) {
        List<MemoImageResponse> imageResponses = ideaContest.getMemoImages().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new IdeaContestResponse(
                ideaContest.getId(),
                ideaContest.getUser().getId(),
                imageResponses
        );
    }

    private MemoImageResponse toResponse(MemoImage memoImage) {
        return new MemoImageResponse(
                memoImage.getId(),
                memoImage.getOriginalFilename(),
                memoImage.getS3Key(),
                s3UploadService.createPresignedDownloadUrl(memoImage.getS3Key()),
                memoImage.getFileSize()
        );
    }

}

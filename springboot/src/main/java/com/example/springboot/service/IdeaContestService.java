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

    public IdeaContestService(
            IdeaContestRepository ideaContestRepository,
            UserRepository userRepository,
            S3UploadService s3UploadService,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.ideaContestRepository = ideaContestRepository;
        this.userRepository = userRepository;
        this.s3UploadService = s3UploadService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public IdeaContestResponse create(String token, List<MultipartFile> images) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("authorization token is required.");
        }
        if (images == null || images.stream().noneMatch(f -> f != null && !f.isEmpty())) {
            throw new IllegalArgumentException("at least one image is required.");
        }

        TokenType tokenType = jwtTokenProvider.extractTokenType(token);
        if (tokenType != TokenType.REGISTER && tokenType != TokenType.VERIFIED) {
            throw new IllegalArgumentException("token type is not valid for idea contest.");
        }

        Long userId = jwtTokenProvider.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found."));

        IdeaContest ideaContest = new IdeaContest();
        ideaContest.setUser(user);

        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                continue;
            }
            String s3Key = s3UploadService.uploadMemoImage(image);

            MemoImage memoImage = new MemoImage();
            memoImage.setOriginalFilename(image.getOriginalFilename() == null ? "unknown" : image.getOriginalFilename());
            memoImage.setS3Key(s3Key);
            memoImage.setFileSize(image.getSize());
            ideaContest.addMemoImage(memoImage);
        }

        IdeaContest saved = ideaContestRepository.save(ideaContest);
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

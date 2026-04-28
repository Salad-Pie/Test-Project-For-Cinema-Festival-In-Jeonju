package com.example.springboot.controller;

import com.example.springboot.dto.IdeaContestResponse;
import com.example.springboot.service.IdeaContestService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/idea-contests")
public class IdeaContestController {

    private final IdeaContestService ideaContestService;

    public IdeaContestController(IdeaContestService ideaContestService) {
        this.ideaContestService = ideaContestService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IdeaContestResponse> create(
            @RequestHeader("Authorization") String authorization,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        String token = authorization.replace("Bearer ", "");
        return ResponseEntity.ok(ideaContestService.create(token, images));
    }
}

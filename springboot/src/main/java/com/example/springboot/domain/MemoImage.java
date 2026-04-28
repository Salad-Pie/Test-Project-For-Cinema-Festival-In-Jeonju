package com.example.springboot.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "memo_images")
public class MemoImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idea_contest_id", nullable = false)
    private IdeaContest ideaContest;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 500)
    private String s3Key;

    @Column(nullable = false)
    private Long fileSize;

    public Long getId() {
        return id;
    }

    public IdeaContest getIdeaContest() {
        return ideaContest;
    }

    public void setIdeaContest(IdeaContest ideaContest) {
        this.ideaContest = ideaContest;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}

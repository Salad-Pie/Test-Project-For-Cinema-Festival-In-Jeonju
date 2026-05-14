package com.example.springboot.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "memo_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemoImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idea_contest_id", nullable = false)
    @JsonIgnore
    private IdeaContest ideaContest;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 500)
    private String s3Key;

    @Column(nullable = false)
    private Long fileSize;
}


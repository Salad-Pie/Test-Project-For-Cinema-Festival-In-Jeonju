package com.example.springboot.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "idea_contests")
public class IdeaContest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "ideaContest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemoImage> memoImages = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<MemoImage> getMemoImages() {
        return memoImages;
    }

    public void addMemoImage(MemoImage memoImage) {
        memoImage.setIdeaContest(this);
        this.memoImages.add(memoImage);
    }
}

package com.example.springboot.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "experience_zone_surveys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceZoneSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30)
    private String phoneNumber;

    @Column(nullable = false, length = 300)
    private String address;

    @Column(nullable = false, columnDefinition = "text")
    private String impressiveSpace;

    @Column(nullable = false, columnDefinition = "text")
    private String improvementIdeaSpace;

    @Column(nullable = false, columnDefinition = "text")
    private String streamingParticipation;

    @Column(nullable = false, columnDefinition = "text")
    private String desiredGoods;

    @Column(nullable = false, columnDefinition = "text")
    private String feedback;

    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}


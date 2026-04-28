package com.example.springboot.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "experience_zone_surveys")
public class ExperienceZoneSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getImpressiveSpace() { return impressiveSpace; }
    public void setImpressiveSpace(String impressiveSpace) { this.impressiveSpace = impressiveSpace; }
    public String getImprovementIdeaSpace() { return improvementIdeaSpace; }
    public void setImprovementIdeaSpace(String improvementIdeaSpace) { this.improvementIdeaSpace = improvementIdeaSpace; }
    public String getStreamingParticipation() { return streamingParticipation; }
    public void setStreamingParticipation(String streamingParticipation) { this.streamingParticipation = streamingParticipation; }
    public String getDesiredGoods() { return desiredGoods; }
    public void setDesiredGoods(String desiredGoods) { this.desiredGoods = desiredGoods; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}


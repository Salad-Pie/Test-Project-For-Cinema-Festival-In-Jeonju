package com.example.springboot.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exhibition_surveys")
public class ExhibitionSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30)
    private String phoneNumber;

    @Column(nullable = false, length = 300)
    private String address;

    @Column(nullable = false, length = 6)
    private String identifierCode;

    @Column(nullable = false, columnDefinition = "text")
    private String impressivePoint;

    @Column(nullable = false, columnDefinition = "text")
    private String improvementNeeded;

    @Column(nullable = false, columnDefinition = "text")
    private String desiredGenre;

    @Column(nullable = false, columnDefinition = "text")
    private String invitedArtist;

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
    public String getIdentifierCode() { return identifierCode; }
    public void setIdentifierCode(String identifierCode) { this.identifierCode = identifierCode; }
    public String getImpressivePoint() { return impressivePoint; }
    public void setImpressivePoint(String impressivePoint) { this.impressivePoint = impressivePoint; }
    public String getImprovementNeeded() { return improvementNeeded; }
    public void setImprovementNeeded(String improvementNeeded) { this.improvementNeeded = improvementNeeded; }
    public String getDesiredGenre() { return desiredGenre; }
    public void setDesiredGenre(String desiredGenre) { this.desiredGenre = desiredGenre; }
    public String getInvitedArtist() { return invitedArtist; }
    public void setInvitedArtist(String invitedArtist) { this.invitedArtist = invitedArtist; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}


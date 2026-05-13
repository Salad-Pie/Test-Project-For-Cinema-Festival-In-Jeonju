package com.example.springboot.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(lombok.AccessLevel.NONE)
    private Long id;

    @Column(length = 255)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "profile_image_url", columnDefinition = "text")
    private String profileImageUrl;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 20)
    private String gender;

    @Column(length = 50)
    private String nationality;

    @Column(nullable = false, length = 10)
    private String language;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 20)
    private String role;

    @Column(name = "is_verified_identity", nullable = false)
    private boolean isVerifiedIdentity;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Transient
    private SignupProvider provider;

    @Transient
    private String providerUserId;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.nickname == null || this.nickname.isBlank()) {
            this.nickname = "nickname";
        }
        if (this.language == null || this.language.isBlank()) {
            this.language = "ko";
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "active";
        }
        if (this.role == null || this.role.isBlank()) {
            this.role = "USER";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // Legacy compatibility methods for current service/controller code.
    public String getPhoneNumber() {
        return phone;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phone = phoneNumber;
    }

    public String getName() {
        return fullName;
    }

    public void setName(String name) {
        this.fullName = name;
    }

    public void setVerifiedAt(java.time.LocalDateTime verifiedAt) {
        this.isVerifiedIdentity = verifiedAt != null;
    }

    public java.time.LocalDateTime getVerifiedAt() {
        return isVerifiedIdentity ? java.time.LocalDateTime.now() : null;
    }

    public boolean isVerified() {
        return isVerifiedIdentity;
    }
}

package com.example.springboot.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sponsorship_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorshipApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30)
    private String phoneNumber;

    @Column(nullable = false, length = 100)
    private String bankAccount;

    @Column(length = 30)
    private String paymentMethodType;

    @Column(length = 100)
    private String paymentProviderName;

    @Column(nullable = false, length = 40)
    private String bankAccountMasked;

    @Column(nullable = false, length = 64)
    private String bankAccountHash;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}


package com.example.springboot.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "street_collaboration_reservations")
public class StreetCollaborationReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDateTime reservationAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getReservationAt() {
        return reservationAt;
    }

    public void setReservationAt(LocalDateTime reservationAt) {
        this.reservationAt = reservationAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

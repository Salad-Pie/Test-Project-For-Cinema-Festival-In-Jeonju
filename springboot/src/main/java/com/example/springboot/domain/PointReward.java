package com.example.springboot.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * 포인트 적립 및 사용 이력을 관리하는 엔티티
 */
@Entity
@Table(name = "point_reward")
@Getter
@Setter
public class PointReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK to user(id) - 주석 처리 및 필드로 유지
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // FK to order(id) - 주석 처리 및 필드로 유지
    @Column(name = "order_id")
    private Long orderId;

    // FK to activity_record(id) - 주석 처리 및 필드로 유지
    @Column(name = "activity_record_id")
    private Long activityRecordId;

    @Column(name = "point_type", nullable = false, length = 20)
    private String pointType; // earn, use, adjust

    @Column(name = "points_delta", nullable = false)
    private Integer pointsDelta;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter = 0;

    @Column(name = "reason_text", length = 255)
    private String reasonText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

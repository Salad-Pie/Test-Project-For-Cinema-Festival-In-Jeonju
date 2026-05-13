package com.example.springboot.repository;

import com.example.springboot.domain.PointReward;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PointRewardRepository extends JpaRepository<PointReward, Long> {
    
    List<PointReward> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 유저의 최신 잔액을 가져옵니다.
     */
    @Query("SELECT p.balanceAfter FROM PointReward p WHERE p.userId = :userId ORDER BY p.id DESC LIMIT 1")
    Optional<Integer> findLastBalanceByUserId(@Param("userId") Long userId);
}

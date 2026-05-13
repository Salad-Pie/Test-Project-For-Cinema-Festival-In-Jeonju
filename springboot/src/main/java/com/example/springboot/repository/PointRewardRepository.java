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
     * 특정 유저의 최신 잔액을 가져옵니다. (Native Query 사용하여 호환성 보장)
     */
    @Query(value = "SELECT p.balance_after FROM point_reward p WHERE p.user_id = :userId ORDER BY p.id DESC LIMIT 1", nativeQuery = true)
    Optional<Integer> findLastBalanceByUserId(@Param("userId") Long userId);
}

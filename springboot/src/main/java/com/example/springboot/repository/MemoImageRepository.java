package com.example.springboot.repository;

import com.example.springboot.domain.MemoImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoImageRepository extends JpaRepository<MemoImage, Long> {
}

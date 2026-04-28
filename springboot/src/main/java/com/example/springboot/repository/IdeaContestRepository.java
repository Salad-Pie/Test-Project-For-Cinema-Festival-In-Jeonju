package com.example.springboot.repository;

import com.example.springboot.domain.IdeaContest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdeaContestRepository extends JpaRepository<IdeaContest, Long> {
}

package com.eduvibe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduvibe.model.RubricScore;

public interface RubricScoreRepository extends JpaRepository<RubricScore, UUID> {

    List<RubricScore> findByGradeId(UUID gradeId);

    List<RubricScore> findByGradeIdIn(List<UUID> gradeIds);

    void deleteByGradeId(UUID gradeId);
}

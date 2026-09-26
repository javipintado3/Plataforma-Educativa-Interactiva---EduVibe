package com.eduvibe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduvibe.model.RubricCriterion;

public interface RubricCriterionRepository extends JpaRepository<RubricCriterion, UUID> {

    List<RubricCriterion> findByRubricIdOrderBySortOrderAsc(UUID rubricId);
}

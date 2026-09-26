package com.eduvibe.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduvibe.model.Rubric;

public interface RubricRepository extends JpaRepository<Rubric, UUID> {

    Optional<Rubric> findByAssignmentId(UUID assignmentId);
}

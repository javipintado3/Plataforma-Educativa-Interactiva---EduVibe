package com.eduvibe.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduvibe.model.ExamAttempt;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, UUID> {

    Optional<ExamAttempt> findByExamIdAndStudentId(UUID examId, UUID studentId);

    long countByExamId(UUID examId);

    /** Todos los intentos de un examen, para la vista de corrección del profesorado. */
    @EntityGraph(attributePaths = "student")
    List<ExamAttempt> findByExamIdOrderByStudentNameAsc(UUID examId);
}

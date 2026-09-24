package com.eduvibe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduvibe.model.ExamQuestion;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, UUID> {

    List<ExamQuestion> findByExamIdOrderBySortOrderAsc(UUID examId);

    long countByExamId(UUID examId);
}

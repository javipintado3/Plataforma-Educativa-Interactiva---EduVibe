package com.eduvibe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduvibe.model.ExamOption;

public interface ExamOptionRepository extends JpaRepository<ExamOption, UUID> {

    List<ExamOption> findByQuestionIdOrderBySortOrderAsc(UUID questionId);

    /** Las opciones de varias preguntas a la vez, para pintar el examen entero sin N+1 consultas. */
    @Query("SELECT o FROM ExamOption o WHERE o.question.id IN :questionIds ORDER BY o.sortOrder ASC")
    List<ExamOption> findByQuestionIdIn(@Param("questionIds") List<UUID> questionIds);
}

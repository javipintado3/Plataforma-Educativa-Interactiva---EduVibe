package com.eduvibe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduvibe.model.ExamAnswer;

public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, UUID> {

    List<ExamAnswer> findByAttemptId(UUID attemptId);

    /** Las respuestas de varios intentos a la vez, para la lista de corrección sin N+1 consultas. */
    @Query("SELECT a FROM ExamAnswer a WHERE a.attempt.id IN :attemptIds")
    List<ExamAnswer> findByAttemptIdIn(@Param("attemptIds") List<UUID> attemptIds);
}

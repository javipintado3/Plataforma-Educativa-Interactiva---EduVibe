package com.eduvibe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduvibe.model.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

    /**
     * Tareas de una clase. Las que tienen plazo van primero y por fecha; las que
     * no lo tienen, al final: NULLS LAST es justo lo que se quiere ver en una
     * lista de trabajo pendiente.
     */
    @Query("""
            SELECT a FROM Assignment a
            WHERE a.schoolClass.id = :classId
            ORDER BY a.dueDate ASC NULLS LAST, a.createdAt DESC
            """)
    List<Assignment> findDeClase(@Param("classId") UUID classId);

    long countBySchoolClassId(UUID classId);

    /** Se usa antes de borrar un tema, para no dejar tareas colgando de él. */
    long countByTopicId(UUID topicId);
}

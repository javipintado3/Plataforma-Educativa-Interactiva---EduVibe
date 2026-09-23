package com.eduvibe.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduvibe.model.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    Optional<Submission> findByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId);

    /** Todas las entregas de una tarea, para la pantalla de corrección. */
    @EntityGraph(attributePaths = { "student", "assignment" })
    List<Submission> findByAssignmentIdOrderByStudentNameAsc(UUID assignmentId);

    /** Las entregas de un alumno en una clase, para su pestaña de notas. */
    @Query("""
            SELECT s FROM Submission s
            WHERE s.student.id = :studentId
              AND s.assignment.schoolClass.id = :classId
            ORDER BY s.assignment.dueDate ASC NULLS LAST
            """)
    List<Submission> findDeAlumnoEnClase(@Param("studentId") UUID studentId,
                                         @Param("classId") UUID classId);

    /** Las entregas de un alumno en varias tareas de golpe, para evitar N+1. */
    @Query("SELECT s FROM Submission s WHERE s.student.id = :studentId AND s.assignment.id IN :assignmentIds")
    List<Submission> findDeAlumnoEnTareas(@Param("studentId") UUID studentId,
                                          @Param("assignmentIds") List<UUID> assignmentIds);

    long countByAssignmentId(UUID assignmentId);
}

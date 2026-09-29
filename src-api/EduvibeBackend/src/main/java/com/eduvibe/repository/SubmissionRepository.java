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

    /** Todas las entregas de una clase de golpe, para la analítica del profesorado. */
    @EntityGraph(attributePaths = { "student", "assignment" })
    @Query("SELECT s FROM Submission s WHERE s.assignment.schoolClass.id = :classId")
    List<Submission> findDeClase(@Param("classId") UUID classId);

    long countByAssignmentId(UUID assignmentId);

    /** Si ya hay alguna entrega en la clase, para no dejar borrarla sin más. */
    boolean existsByAssignmentSchoolClassId(UUID classId);

    /**
     * Las otras entregas del mismo subgrupo en la misma tarea, para propagarles
     * lo que se acaba de guardar o calificar sin tocar la entrega de origen.
     */
    List<Submission> findByAssignmentIdAndClassGroupIdAndIdNot(UUID assignmentId, UUID classGroupId, UUID exceptId);

    /** Para el resumen de perfil del profesorado: cuánto trabajo tiene por corregir. */
    @Query("""
            SELECT COUNT(s) FROM Submission s
            WHERE s.status = com.eduvibe.model.enums.SubmissionStatus.SUBMITTED
              AND s.assignment.schoolClass.id IN (
                  SELECT e.schoolClass.id FROM Enrollment e
                  WHERE e.user.id = :teacherId
                    AND e.roleInClass = com.eduvibe.model.enums.EnrollmentRole.TEACHER)
            """)
    long countPorCorregirDeProfesor(@Param("teacherId") UUID teacherId);

    /**
     * Las entregas enviadas y sin corregir de todas las clases que imparte el
     * profesor, las más antiguas primero: es la cola de trabajo que tiene que
     * vaciar, y lo que lleva más tiempo esperando va delante.
     */
    @EntityGraph(attributePaths = { "student", "assignment", "assignment.schoolClass", "classGroup" })
    @Query("""
            SELECT s FROM Submission s
            WHERE s.status = com.eduvibe.model.enums.SubmissionStatus.SUBMITTED
              AND s.assignment.schoolClass.id IN (
                  SELECT e.schoolClass.id FROM Enrollment e
                  WHERE e.user.id = :teacherId
                    AND e.roleInClass = com.eduvibe.model.enums.EnrollmentRole.TEACHER)
            ORDER BY s.submittedAt ASC
            """)
    List<Submission> findPorCorregirDeProfesor(@Param("teacherId") UUID teacherId);
}

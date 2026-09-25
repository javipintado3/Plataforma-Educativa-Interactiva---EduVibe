package com.eduvibe.dto.exam;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.eduvibe.model.Exam;

/**
 * Examen tal y como aparece en la lista de la clase.
 *
 * Igual que {@link com.eduvibe.dto.assignment.AssignmentResponse}: al
 * alumnado le importa el estado de su propio intento, al profesorado cuántos
 * ha recibido. El que no aplica viaja a null.
 */
public record ExamResponse(
        UUID id,
        UUID classId,
        UUID topicId,
        String title,
        int durationMinutes,
        Instant dueDate,
        boolean haVencido,
        int numeroPreguntas,
        String miEstado,
        BigDecimal miNota,
        Long intentosRecibidos,
        Instant createdAt) {

    /** Vista de alumno: el estado de su propio intento ("no_empezado", "en_curso", "entregado"). */
    public static ExamResponse paraAlumno(Exam examen, int numeroPreguntas, String miEstado, BigDecimal miNota) {
        return new ExamResponse(
                examen.getId(),
                examen.getSchoolClass().getId(),
                examen.getTopic() == null ? null : examen.getTopic().getId(),
                examen.getTitle(),
                examen.getDurationMinutes(),
                examen.getDueDate(),
                examen.haVencido(),
                numeroPreguntas,
                miEstado,
                miNota,
                null,
                examen.getCreatedAt());
    }

    /** Vista de profesor: cuántos intentos lleva recibidos. */
    public static ExamResponse paraProfesor(Exam examen, int numeroPreguntas, long intentosRecibidos) {
        return new ExamResponse(
                examen.getId(),
                examen.getSchoolClass().getId(),
                examen.getTopic() == null ? null : examen.getTopic().getId(),
                examen.getTitle(),
                examen.getDurationMinutes(),
                examen.getDueDate(),
                examen.haVencido(),
                numeroPreguntas,
                null,
                null,
                intentosRecibidos,
                examen.getCreatedAt());
    }
}

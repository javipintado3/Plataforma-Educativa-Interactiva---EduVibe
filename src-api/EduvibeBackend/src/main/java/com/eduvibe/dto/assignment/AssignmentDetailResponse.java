package com.eduvibe.dto.assignment;

import java.time.Instant;
import java.util.UUID;

import com.eduvibe.dto.submission.SubmissionResponse;
import com.eduvibe.model.Assignment;

/**
 * Pantalla de una tarea.
 *
 * @param puedoEditar si quien consulta puede modificarla o corregir entregas
 * @param miEntrega   la entrega de quien consulta, si es alumno; null si no
 */
public record AssignmentDetailResponse(
        UUID id,
        UUID classId,
        String className,
        String classSubject,
        UUID topicId,
        String title,
        String description,
        Instant dueDate,
        int points,
        boolean haVencido,
        boolean puedoEditar,
        SubmissionResponse miEntrega,
        Instant createdAt) {

    public static AssignmentDetailResponse de(Assignment tarea, boolean puedoEditar,
                                              SubmissionResponse miEntrega) {
        return new AssignmentDetailResponse(
                tarea.getId(),
                tarea.getSchoolClass().getId(),
                tarea.getSchoolClass().getName(),
                tarea.getSchoolClass().getSubject(),
                tarea.getTopic() == null ? null : tarea.getTopic().getId(),
                tarea.getTitle(),
                tarea.getDescription(),
                tarea.getDueDate(),
                tarea.getPoints(),
                tarea.haVencido(),
                puedoEditar,
                miEntrega,
                tarea.getCreatedAt());
    }
}

package com.eduvibe.dto.assignment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.eduvibe.dto.classgroup.ClassGroupResponse;
import com.eduvibe.dto.rubric.RubricResponse;
import com.eduvibe.dto.submission.SubmissionResponse;
import com.eduvibe.model.Assignment;

/**
 * Pantalla de una tarea.
 *
 * @param puedoEditar si quien consulta puede modificarla o corregir entregas
 * @param miEntrega   la entrega de quien consulta, si es alumno; null si no
 * @param rubric      su rúbrica, si tiene; null si se califica con una nota suelta
 * @param myGroup     el subgrupo de quien consulta, en una tarea grupal; null si
 *                    no es grupal, si consulta el profesorado, o si el alumno
 *                    todavía no está en ningún subgrupo de la clase
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
        int latePenaltyPercent,
        BigDecimal weight,
        boolean groupAssignment,
        boolean haVencido,
        boolean puedoEditar,
        SubmissionResponse miEntrega,
        RubricResponse rubric,
        ClassGroupResponse myGroup,
        Instant createdAt) {

    public static AssignmentDetailResponse de(Assignment tarea, boolean puedoEditar,
                                              SubmissionResponse miEntrega, RubricResponse rubric,
                                              ClassGroupResponse myGroup) {
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
                tarea.getLatePenaltyPercent(),
                tarea.getWeight(),
                tarea.isGroupAssignment(),
                tarea.haVencido(),
                puedoEditar,
                miEntrega,
                rubric,
                myGroup,
                tarea.getCreatedAt());
    }
}

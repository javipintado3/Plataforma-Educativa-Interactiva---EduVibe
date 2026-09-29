package com.eduvibe.dto.submission;

import java.time.Instant;
import java.util.UUID;

import com.eduvibe.model.Submission;

/**
 * Una entrega a la espera de corrección, con lo justo para saber qué es y
 * llevar al profesorado a corregirla, sin traer el contenido entregado.
 *
 * @param groupName el subgrupo que la entregó, en una tarea grupal; en ese caso
 *                  {@code studentName} es solo uno de sus miembros, porque se
 *                  lista una fila por subgrupo y no una por alumno
 */
public record EntregaPorCorregirResponse(
        UUID submissionId,
        UUID assignmentId,
        String assignmentTitle,
        UUID classId,
        String className,
        String studentName,
        String groupName,
        Instant submittedAt,
        boolean entregadaTarde) {

    public static EntregaPorCorregirResponse de(Submission entrega) {
        return new EntregaPorCorregirResponse(
                entrega.getId(),
                entrega.getAssignment().getId(),
                entrega.getAssignment().getTitle(),
                entrega.getAssignment().getSchoolClass().getId(),
                entrega.getAssignment().getSchoolClass().getName(),
                entrega.getStudent().getName(),
                entrega.getClassGroup() == null ? null : entrega.getClassGroup().getName(),
                entrega.getSubmittedAt(),
                entrega.entregadaTarde());
    }
}

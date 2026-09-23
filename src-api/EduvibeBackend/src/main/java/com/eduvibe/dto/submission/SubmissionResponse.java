package com.eduvibe.dto.submission;

import java.time.Instant;
import java.util.UUID;

import com.eduvibe.model.Submission;

/**
 * Una entrega, con su estado y su nota si ya está corregida.
 *
 * @param entregadaTarde se calcula comparando la fecha de envío con el plazo de
 *                       la tarea; no está guardado en ninguna columna, y por
 *                       eso una entrega puede estar calificada y tarde a la vez
 * @param grade          null mientras no se haya corregido
 */
public record SubmissionResponse(
        UUID id,
        UUID assignmentId,
        String assignmentTitle,
        UUID studentId,
        String studentName,
        String content,
        String fileUrl,
        String status,
        Instant submittedAt,
        boolean entregadaTarde,
        GradeResponse grade) {

    public static SubmissionResponse de(Submission entrega, GradeResponse nota) {
        return new SubmissionResponse(
                entrega.getId(),
                entrega.getAssignment().getId(),
                entrega.getAssignment().getTitle(),
                entrega.getStudent().getId(),
                entrega.getStudent().getName(),
                entrega.getContent(),
                entrega.getFileUrl(),
                entrega.getStatus().getValor(),
                entrega.getSubmittedAt(),
                entrega.entregadaTarde(),
                nota);
    }
}

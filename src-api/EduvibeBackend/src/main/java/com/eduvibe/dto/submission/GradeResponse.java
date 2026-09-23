package com.eduvibe.dto.submission;

import java.math.BigDecimal;
import java.time.Instant;

import com.eduvibe.model.Grade;

/**
 * Calificación de una entrega.
 *
 * Lleva quién corrige y cuándo porque una nota, en un centro educativo, es un
 * acto con responsable.
 */
public record GradeResponse(
        BigDecimal score,
        String feedback,
        String gradedByName,
        Instant gradedAt) {

    public static GradeResponse de(Grade nota) {
        return new GradeResponse(
                nota.getScore(),
                nota.getFeedback(),
                nota.getGradedBy().getName(),
                nota.getGradedAt());
    }
}

package com.eduvibe.dto.analytics;

import java.math.BigDecimal;
import java.time.Instant;

/** Un punto de la gráfica de evolución de notas de un alumno: una tarea calificada. */
public record GradePointResponse(
        String assignmentTitle,
        Instant gradedAt,
        BigDecimal percent) {
}

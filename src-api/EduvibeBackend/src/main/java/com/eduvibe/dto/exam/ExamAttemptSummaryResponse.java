package com.eduvibe.dto.exam;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Una fila de la vista de corrección del profesorado: un intento de un alumno. */
public record ExamAttemptSummaryResponse(
        UUID attemptId,
        UUID studentId,
        String studentName,
        Instant startedAt,
        Instant submittedAt,
        BigDecimal score,
        boolean entregado) {
}

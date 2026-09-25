package com.eduvibe.dto.exam;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Resultado de un intento ya entregado, con el desglose pregunta a pregunta. */
public record ExamResultResponse(
        UUID attemptId,
        BigDecimal score,
        int totalPuntos,
        Instant submittedAt,
        List<QuestionResultResponse> preguntas) {

    public record QuestionResultResponse(UUID questionId, String text, int points,
                                          UUID miRespuesta, UUID respuestaCorrecta, boolean acerto) {
    }
}

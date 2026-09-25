package com.eduvibe.dto.exam;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * El examen tal y como lo ve el alumnado mientras lo hace: sus preguntas sin
 * marcar cuál opción es correcta, más lo que ya haya respondido si recarga la
 * página o retoma un intento en curso.
 */
public record ExamAttemptResponse(
        UUID attemptId,
        UUID examId,
        String examTitle,
        int durationMinutes,
        Instant startedAt,
        Instant deadline,
        boolean entregado,
        List<QuestionResponse> questions) {

    public record QuestionResponse(UUID id, String text, int points, List<OptionResponse> options,
                                    UUID miRespuesta) {
    }

    public record OptionResponse(UUID id, String text) {
    }
}

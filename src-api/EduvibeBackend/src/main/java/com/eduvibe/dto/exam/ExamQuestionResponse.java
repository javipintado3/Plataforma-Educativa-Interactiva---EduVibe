package com.eduvibe.dto.exam;

import java.util.List;
import java.util.UUID;

import com.eduvibe.model.ExamOption;
import com.eduvibe.model.ExamQuestion;

/**
 * Una pregunta con sus opciones y cuál es la correcta.
 *
 * Solo para el profesorado ({@code GET /api/exams/{id}/questions}), que es
 * quien puede ver la corrección. Lo que responde el alumnado mientras hace el
 * examen es {@link ExamAttemptResponse}, sin este dato.
 */
public record ExamQuestionResponse(UUID id, String text, int points, List<OptionResponse> options) {

    public static ExamQuestionResponse de(ExamQuestion pregunta, List<ExamOption> opciones) {
        return new ExamQuestionResponse(
                pregunta.getId(),
                pregunta.getText(),
                pregunta.getPoints(),
                opciones.stream().map(OptionResponse::de).toList());
    }

    public record OptionResponse(UUID id, String text, boolean correct) {

        public static OptionResponse de(ExamOption opcion) {
            return new OptionResponse(opcion.getId(), opcion.getText(), opcion.isCorrect());
        }
    }
}

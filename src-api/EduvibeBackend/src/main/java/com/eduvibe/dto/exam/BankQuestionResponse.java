package com.eduvibe.dto.exam;

import java.util.List;
import java.util.UUID;

import com.eduvibe.model.ExamOption;
import com.eduvibe.model.ExamQuestion;

/**
 * Una pregunta ya usada en algún examen de la clase, para reutilizarla en uno
 * nuevo. Lleva la respuesta correcta marcada porque solo se ofrece al
 * profesorado, igual que {@link ExamQuestionResponse}.
 */
public record BankQuestionResponse(UUID id, String text, int points, String examTitle, List<OptionResponse> options) {

    public static BankQuestionResponse de(ExamQuestion pregunta, List<ExamOption> opciones) {
        return new BankQuestionResponse(
                pregunta.getId(),
                pregunta.getText(),
                pregunta.getPoints(),
                pregunta.getExam().getTitle(),
                opciones.stream().map(OptionResponse::de).toList());
    }

    public record OptionResponse(UUID id, String text, boolean correct) {

        public static OptionResponse de(ExamOption opcion) {
            return new OptionResponse(opcion.getId(), opcion.getText(), opcion.isCorrect());
        }
    }
}

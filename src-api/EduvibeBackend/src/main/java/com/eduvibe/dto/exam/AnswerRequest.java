package com.eduvibe.dto.exam;

import java.util.UUID;

/** Qué opción marca el alumno para una pregunta. Null para dejarla sin responder. */
public record AnswerRequest(UUID selectedOptionId) {
}

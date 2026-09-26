package com.eduvibe.dto.rubric;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Lo puntuado en un criterio concreto, dentro de {@link com.eduvibe.dto.submission.GradeRequest}. */
public record RubricScoreInput(

        @NotNull(message = "Falta el criterio")
        UUID criterionId,

        @NotNull(message = "La puntuación del criterio es obligatoria")
        @DecimalMin(value = "0.0", message = "La puntuación no puede ser negativa")
        BigDecimal points) {
}

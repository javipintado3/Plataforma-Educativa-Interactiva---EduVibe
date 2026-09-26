package com.eduvibe.dto.rubric;

import java.math.BigDecimal;
import java.util.UUID;

import com.eduvibe.model.RubricScore;

public record RubricScoreResponse(
        UUID criterionId,
        String description,
        BigDecimal maxPoints,
        BigDecimal points) {

    public static RubricScoreResponse de(RubricScore puntuacion) {
        return new RubricScoreResponse(
                puntuacion.getCriterion().getId(),
                puntuacion.getCriterion().getDescription(),
                puntuacion.getCriterion().getMaxPoints(),
                puntuacion.getPoints());
    }
}

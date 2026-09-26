package com.eduvibe.dto.rubric;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.eduvibe.model.Rubric;
import com.eduvibe.model.RubricCriterion;

public record RubricResponse(
        UUID id,
        List<CriterionResponse> criteria) {

    public static RubricResponse de(Rubric rubrica, List<RubricCriterion> criterios) {
        return new RubricResponse(
                rubrica.getId(),
                criterios.stream().map(CriterionResponse::de).toList());
    }

    public record CriterionResponse(
            UUID id,
            String description,
            BigDecimal maxPoints) {

        public static CriterionResponse de(RubricCriterion criterio) {
            return new CriterionResponse(criterio.getId(), criterio.getDescription(), criterio.getMaxPoints());
        }
    }
}

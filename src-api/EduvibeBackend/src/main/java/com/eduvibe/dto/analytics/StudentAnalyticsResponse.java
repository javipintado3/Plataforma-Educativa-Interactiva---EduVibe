package com.eduvibe.dto.analytics;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Cómo va un alumno concreto en la clase: cuánto ha entregado, su media y si
 * está en riesgo.
 *
 * @param averageScore media ponderada en porcentaje de lo que ya tiene
 *                     calificado; null si todavía no tiene ninguna nota
 * @param atRisk       si acumula varias tareas ya vencidas sin entregar
 */
public record StudentAnalyticsResponse(
        UUID userId,
        String name,
        long submittedCount,
        long pendingCount,
        int totalAssignments,
        int completionPercent,
        BigDecimal averageScore,
        boolean atRisk,
        List<GradePointResponse> grades) {
}

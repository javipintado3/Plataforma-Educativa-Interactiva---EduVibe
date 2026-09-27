package com.eduvibe.dto.analytics;

import java.util.List;

/**
 * Panel de analítica de una clase: quién ha entregado, quién no, y quién está
 * en riesgo por acumular tareas vencidas sin entregar.
 */
public record ClassAnalyticsResponse(
        int classSize,
        int overallCompletionPercent,
        long atRiskCount,
        List<AssignmentAnalyticsResponse> assignments,
        List<StudentAnalyticsResponse> students) {
}

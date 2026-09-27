package com.eduvibe.dto.analytics;

import java.time.Instant;
import java.util.UUID;

/** Cuánta gente ha entregado una tarea concreta, para el panel del profesorado. */
public record AssignmentAnalyticsResponse(
        UUID id,
        String title,
        Instant dueDate,
        long submittedCount,
        long pendingCount,
        int completionPercent) {
}

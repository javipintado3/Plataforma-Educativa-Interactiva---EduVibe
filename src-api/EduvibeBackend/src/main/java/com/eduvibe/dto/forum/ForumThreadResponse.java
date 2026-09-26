package com.eduvibe.dto.forum;

import java.time.Instant;
import java.util.UUID;

import com.eduvibe.model.ForumThread;

/**
 * Un hilo en la lista del foro de una clase.
 *
 * @param lastActivityAt cuándo se escribió el último mensaje; si nadie ha
 *                        respondido todavía, es la fecha del propio hilo
 */
public record ForumThreadResponse(
        UUID id,
        UUID topicId,
        String title,
        String authorName,
        long postCount,
        Instant lastActivityAt,
        Instant createdAt) {

    public static ForumThreadResponse de(ForumThread hilo, long postCount, Instant lastActivityAt) {
        return new ForumThreadResponse(
                hilo.getId(),
                hilo.getTopic() == null ? null : hilo.getTopic().getId(),
                hilo.getTitle(),
                hilo.getAuthor().getName(),
                postCount,
                lastActivityAt != null ? lastActivityAt : hilo.getCreatedAt(),
                hilo.getCreatedAt());
    }
}

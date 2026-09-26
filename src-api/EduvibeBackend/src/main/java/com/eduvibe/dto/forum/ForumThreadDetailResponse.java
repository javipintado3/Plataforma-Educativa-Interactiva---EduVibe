package com.eduvibe.dto.forum;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.eduvibe.model.ForumThread;

/**
 * @param puedoModerar si quien consulta puede borrar cualquier mensaje o el
 *                      hilo entero, aunque no sea suyo: el profesorado de la
 *                      clase
 */
public record ForumThreadDetailResponse(
        UUID id,
        UUID classId,
        String className,
        UUID topicId,
        String title,
        String authorName,
        boolean puedoModerar,
        boolean puedoBorrarHilo,
        List<ForumPostResponse> posts,
        Instant createdAt) {

    public static ForumThreadDetailResponse de(ForumThread hilo, boolean puedoModerar, boolean puedoBorrarHilo,
                                               List<ForumPostResponse> posts) {
        return new ForumThreadDetailResponse(
                hilo.getId(),
                hilo.getSchoolClass().getId(),
                hilo.getSchoolClass().getName(),
                hilo.getTopic() == null ? null : hilo.getTopic().getId(),
                hilo.getTitle(),
                hilo.getAuthor().getName(),
                puedoModerar,
                puedoBorrarHilo,
                posts,
                hilo.getCreatedAt());
    }
}

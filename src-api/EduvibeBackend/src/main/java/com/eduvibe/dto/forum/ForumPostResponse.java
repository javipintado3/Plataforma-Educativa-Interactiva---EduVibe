package com.eduvibe.dto.forum;

import java.time.Instant;
import java.util.UUID;

import com.eduvibe.model.ForumPost;

/**
 * @param puedoBorrar si quien consulta puede borrarlo: su autor, o el
 *                    profesorado de la clase como moderación
 */
public record ForumPostResponse(
        UUID id,
        String content,
        String authorName,
        Instant createdAt,
        boolean puedoBorrar) {

    public static ForumPostResponse de(ForumPost mensaje, boolean puedoBorrar) {
        return new ForumPostResponse(
                mensaje.getId(),
                mensaje.getContent(),
                mensaje.getAuthor().getName(),
                mensaje.getCreatedAt(),
                puedoBorrar);
    }
}

package com.eduvibe.dto.announcement;

import java.time.Instant;
import java.util.UUID;

import com.eduvibe.model.Announcement;

/**
 * Aviso del muro de una clase.
 *
 * @param puedoBorrar si quien consulta puede retirarlo; lo decide el servicio,
 *                    no el cliente, y así el botón solo aparece cuando la
 *                    acción va a funcionar
 */
public record AnnouncementResponse(
        UUID id,
        String content,
        boolean pinned,
        String authorName,
        Instant createdAt,
        boolean puedoBorrar) {

    public static AnnouncementResponse de(Announcement aviso, boolean puedoBorrar) {
        return new AnnouncementResponse(
                aviso.getId(),
                aviso.getContent(),
                aviso.isPinned(),
                aviso.getAuthor().getName(),
                aviso.getCreatedAt(),
                puedoBorrar);
    }
}

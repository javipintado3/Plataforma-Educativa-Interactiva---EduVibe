package com.eduvibe.dto.auth;

import java.time.Instant;

/**
 * Datos que se muestran al abrir un enlace de invitación, antes de establecer
 * la contraseña, para que la persona vea a qué cuenta corresponde.
 *
 * No lleva identificadores: quien tiene el enlace todavía no está autenticado.
 */
public record InvitationInfoResponse(
        String name,
        String email,
        String organizationName,
        Instant expiresAt) {
}

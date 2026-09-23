package com.eduvibe.dto.user;

import java.time.Instant;

/**
 * Resultado de emitir una invitación.
 *
 * El enlace se devuelve también en la respuesta, no solo por correo. Es
 * deliberado: permite que el panel de administración lo muestre y que la
 * aplicación siga siendo utilizable aunque el servidor de correo no esté
 * configurado, que en un despliegue de demostración es lo habitual.
 *
 * @param link      enlace de aceptación, con el token en claro
 * @param expiresAt momento en que deja de servir
 * @param emailEnviado si se ha podido enviar el correo
 */
public record InvitationResponse(
        String link,
        Instant expiresAt,
        boolean emailEnviado) {
}

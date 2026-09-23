package com.eduvibe.dto.user;

/**
 * Resultado de dar de alta a un usuario: la cuenta creada y la invitación
 * emitida para ella.
 *
 * Van juntas porque el panel de administración necesita las dos cosas a la vez:
 * la fila que añadir a la tabla y el enlace que mostrar si el correo no ha
 * salido.
 */
public record CreateUserResponse(
        UserResponse user,
        InvitationResponse invitation) {
}

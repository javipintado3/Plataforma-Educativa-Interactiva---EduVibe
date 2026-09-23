package com.eduvibe.security;

import java.util.UUID;

import com.eduvibe.model.enums.UserRole;

/**
 * Identidad de quien hace la petición, reconstruida a partir del token.
 *
 * Es lo que queda como principal en el contexto de seguridad, de modo que un
 * controlador o un servicio pueden saber quién es y de qué organización sin
 * volver a consultar la base de datos en cada petición.
 */
public record AuthenticatedUser(
        UUID id,
        String email,
        String name,
        UserRole role,
        UUID organizationId) {

    public boolean esAdmin() {
        return role == UserRole.ADMIN;
    }
}

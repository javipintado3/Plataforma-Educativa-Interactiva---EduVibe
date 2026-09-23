package com.eduvibe.dto.auth;

import java.time.Instant;

import com.eduvibe.dto.user.UserResponse;

/**
 * Respuesta de un inicio de sesión correcto.
 *
 * Incluye el usuario para que el cliente no tenga que descodificar el token
 * para pintar el nombre o decidir qué menú mostrar, y la fecha de caducidad
 * para que pueda avisar antes de que expire sin hacer esa descodificación.
 *
 * @param token     token de acceso, sin el prefijo "Bearer"
 * @param expiresAt momento en que el token deja de ser válido
 * @param user      datos de la persona que ha entrado
 */
public record AuthResponse(
        String token,
        Instant expiresAt,
        UserResponse user) {
}

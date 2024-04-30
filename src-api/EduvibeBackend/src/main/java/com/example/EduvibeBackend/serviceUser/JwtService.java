package com.example.EduvibeBackend.serviceUser;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Interfaz para el servicio de manipulación de tokens JWT.
 */
public interface JwtService {

    /**
     * Extrae el nombre de usuario del token JWT.
     *
     * @param token Token JWT del cual extraer el nombre de usuario.
     * @return Nombre de usuario extraído del token.
     */
    String extractUserName(String token);

    /**
     * Genera un token JWT para el usuario proporcionado.
     *
     * @param userDetails Detalles del usuario para los cuales se generará el token.
     * @return Token JWT generado.
     */
    String generateToken(UserDetails userDetails);

    /**
     * Verifica si un token JWT es válido para los detalles del usuario proporcionados.
     *
     * @param token       Token JWT a verificar.
     * @param userDetails Detalles del usuario para los cuales se verificará el token.
     * @return `true` si el token es válido, `false` en caso contrario.
     */
    boolean isTokenValid(String token, UserDetails userDetails);
}
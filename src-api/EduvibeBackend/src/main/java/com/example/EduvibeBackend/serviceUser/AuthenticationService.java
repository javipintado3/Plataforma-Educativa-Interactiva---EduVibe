package com.example.EduvibeBackend.serviceUser;

import com.example.EduvibeBackend.dto.request.SignUpRequest;
import com.example.EduvibeBackend.dto.request.SigninRequest;
import com.example.EduvibeBackend.dto.response.JwtAuthenticationResponse;
import com.example.EduvibeBackend.entities.Role;

/**
 * Interfaz para el servicio de autenticación.
 */
public interface AuthenticationService {
    
    /**
     * Registro de un nuevo usuario y generación del token JWT correspondiente.
     *
     * @param request Detalles de registro del usuario.
     * @return Respuesta que contiene el token JWT.
     */
    JwtAuthenticationResponse signup(SignUpRequest request, Role rol);

    /**
     * Autenticación de un usuario y generación del token JWT correspondiente.
     *
     * @param request Detalles de inicio de sesión del usuario.
     * @return Respuesta que contiene el token JWT.
     */
    JwtAuthenticationResponse signin(SigninRequest request);
}
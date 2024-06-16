package com.example.EduvibeBackend.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa una solicitud de registro.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * Nombre completo del usuario.
     */
    String nombreCompleto;

    /**
     * Dirección de correo electrónico del usuario.
     */
    String email;

    /**
     * Contraseña del usuario.
     */
    String password;

    /**
     * Rol del usuario (por ejemplo, alumno, profesor, administrador).
     */
    String rol;
}

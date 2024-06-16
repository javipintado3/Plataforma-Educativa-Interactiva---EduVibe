package com.example.EduvibeBackend.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa una solicitud de inicio de sesión.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    /**
     * Dirección de correo electrónico del usuario.
     */
    String email;

    /**
     * Contraseña del usuario.
     */
    String password; 
}

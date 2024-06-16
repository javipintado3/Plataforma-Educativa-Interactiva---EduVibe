package com.example.EduvibeBackend.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa los datos de inicio de sesión de un usuario.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLogin {

    /**
     * Correo electrónico del usuario.
     */
    private String email;

    /**
     * Contraseña del usuario.
     */
    private String password;
}

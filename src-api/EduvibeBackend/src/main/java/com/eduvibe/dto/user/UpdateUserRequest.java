package com.eduvibe.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Edición de una cuenta ya existente desde el panel de administración.
 *
 * Mismas reglas que {@link CreateUserRequest}: no toca la contraseña ni el
 * estado, que tienen sus propios flujos (invitación y activar/desactivar).
 */
public record UpdateUserRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String name,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        @Size(max = 255, message = "El email no puede superar los 255 caracteres")
        String email,

        @NotBlank(message = "El rol es obligatorio")
        @Pattern(regexp = "admin|teacher|student|guardian",
                 message = "El rol debe ser admin, teacher, student o guardian")
        String role) {
}

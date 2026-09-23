package com.eduvibe.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Alta de un usuario por parte de un administrador.
 *
 * No incluye contraseña a propósito: la cuenta nace sin ella y es la persona
 * quien la establece al aceptar su invitación. Nunca se envía una contraseña
 * por correo.
 */
public record CreateUserRequest(

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

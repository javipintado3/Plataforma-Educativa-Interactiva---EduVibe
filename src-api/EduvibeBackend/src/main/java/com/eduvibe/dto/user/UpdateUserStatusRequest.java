package com.eduvibe.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Activación o desactivación de una cuenta.
 *
 * Solo admite 'active' y 'disabled': a 'pending' no se vuelve, porque ese
 * estado solo existe entre el alta y la aceptación de la invitación.
 */
public record UpdateUserStatusRequest(

        @NotBlank(message = "El estado es obligatorio")
        @Pattern(regexp = "active|disabled", message = "El estado debe ser active o disabled")
        String status) {
}

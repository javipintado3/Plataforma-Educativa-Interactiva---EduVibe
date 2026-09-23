package com.eduvibe.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Contraseña que la persona establece al aceptar su invitación.
 */
public record AcceptInvitationRequest(

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener al menos 8 caracteres")
        String password) {
}

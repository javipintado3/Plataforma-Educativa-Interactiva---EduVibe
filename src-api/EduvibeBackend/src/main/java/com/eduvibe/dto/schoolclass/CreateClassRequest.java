package com.eduvibe.dto.schoolclass;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateClassRequest(

        @NotBlank(message = "El nombre de la clase es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String name,

        @Size(max = 120, message = "La asignatura no puede superar los 120 caracteres")
        String subject,

        /** Color en hexadecimal, del estilo #2563eb. */
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "El color debe ser hexadecimal, por ejemplo #2563eb")
        String color) {
}

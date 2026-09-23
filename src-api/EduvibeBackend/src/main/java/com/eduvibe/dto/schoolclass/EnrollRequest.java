package com.eduvibe.dto.schoolclass;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Matriculación de una persona en una clase.
 */
public record EnrollRequest(

        @NotNull(message = "Hay que indicar el usuario")
        UUID userId,

        @NotBlank(message = "Hay que indicar el papel dentro de la clase")
        @Pattern(regexp = "teacher|student", message = "El papel debe ser teacher o student")
        String roleInClass) {
}

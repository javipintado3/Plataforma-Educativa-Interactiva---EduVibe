package com.eduvibe.dto.submission;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Calificación que pone el profesorado.
 *
 * El máximo no se valida aquí sino en el servicio, porque depende de los puntos
 * que valga cada tarea y eso no se sabe hasta consultarla.
 */
public record GradeRequest(

        @NotNull(message = "La nota es obligatoria")
        @DecimalMin(value = "0.0", message = "La nota no puede ser negativa")
        @Digits(integer = 3, fraction = 2, message = "La nota admite como mucho dos decimales")
        BigDecimal score,

        @Size(max = 5000, message = "El comentario es demasiado largo")
        String feedback) {
}

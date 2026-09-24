package com.eduvibe.dto.exam;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Un examen completo, con sus preguntas y opciones, tal y como se crea o se
 * edita de una sola vez: no tiene sentido dar de alta un examen sin
 * preguntas y añadírselas después una a una.
 */
public record SaveExamRequest(

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String title,

        @Size(max = 20000, message = "El enunciado es demasiado largo")
        String description,

        @NotNull(message = "La duración es obligatoria")
        @Positive(message = "La duración debe ser mayor que cero")
        @Max(value = 480, message = "La duración no puede pasar de 480 minutos")
        Integer durationMinutes,

        Instant dueDate,

        UUID topicId,

        @NotEmpty(message = "El examen necesita al menos una pregunta")
        @Valid
        List<QuestionInput> questions) {

    public record QuestionInput(

            @NotBlank(message = "La pregunta no puede estar vacía")
            @Size(max = 2000, message = "La pregunta es demasiado larga")
            String text,

            @Positive(message = "Los puntos deben ser mayores que cero")
            Integer points,

            @NotEmpty(message = "Cada pregunta necesita al menos dos opciones")
            @Size(min = 2, message = "Cada pregunta necesita al menos dos opciones")
            @Valid
            List<OptionInput> options) {

        public int puntosOPorDefecto() {
            return points == null ? 1 : points;
        }
    }

    public record OptionInput(

            @NotBlank(message = "La opción no puede estar vacía")
            @Size(max = 500, message = "La opción es demasiado larga")
            String text,

            boolean correct) {
    }
}

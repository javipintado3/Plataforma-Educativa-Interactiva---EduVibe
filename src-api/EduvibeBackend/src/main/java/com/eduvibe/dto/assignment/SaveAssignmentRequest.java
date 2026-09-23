package com.eduvibe.dto.assignment;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos de una tarea al crearla o al editarla.
 *
 * Se usa el mismo DTO para las dos operaciones porque los campos y sus reglas
 * son exactamente los mismos; tener dos copias idénticas solo garantizaría que
 * algún día se cambie una y no la otra.
 *
 * @param dueDate fecha límite. Null significa que la tarea no tiene plazo, no
 *                que se deje como estaba.
 * @param topicId tema al que pertenece. Null la deja suelta.
 */
public record SaveAssignmentRequest(

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String title,

        @Size(max = 20000, message = "El enunciado es demasiado largo")
        String description,

        Instant dueDate,

        @Positive(message = "La puntuación debe ser mayor que cero")
        @Max(value = 1000, message = "La puntuación no puede pasar de 1000")
        Integer points,

        UUID topicId) {

    /** Si no se indica puntuación, se toma la de un examen al uso. */
    public int puntosOPorDefecto() {
        return points == null ? 100 : points;
    }
}

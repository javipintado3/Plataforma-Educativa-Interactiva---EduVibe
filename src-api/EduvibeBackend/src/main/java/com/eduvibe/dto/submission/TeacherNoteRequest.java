package com.eduvibe.dto.submission;

import jakarta.validation.constraints.Size;

/**
 * Nota rápida del profesorado sobre una entrega, sin calificarla: por
 * ejemplo, "revisa el segundo apartado" antes de que el alumno la retoque.
 */
public record TeacherNoteRequest(

        @Size(max = 2000, message = "La nota es demasiado larga")
        String teacherNote) {
}

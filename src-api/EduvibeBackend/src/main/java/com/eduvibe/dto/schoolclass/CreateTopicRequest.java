package com.eduvibe.dto.schoolclass;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateTopicRequest(

        @NotBlank(message = "El título del tema es obligatorio")
        @Size(max = 120, message = "El título no puede superar los 120 caracteres")
        String title,

        @PositiveOrZero(message = "El orden no puede ser negativo")
        Integer sortOrder) {
}

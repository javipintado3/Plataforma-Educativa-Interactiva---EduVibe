package com.eduvibe.dto.calendar;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveCalendarEventRequest(

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String title,

        @NotNull(message = "La fecha es obligatoria")
        Instant eventDate,

        @Pattern(regexp = "exam|holiday|other", message = "El tipo debe ser exam, holiday u other")
        String type,

        /** Null para un evento de todo el centro. */
        UUID classId) {
}

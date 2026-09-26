package com.eduvibe.dto.forum;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Abre un hilo: título y primer mensaje a la vez, como cualquier hilo de un foro. */
public record SaveForumThreadRequest(

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String title,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = 10000, message = "El mensaje es demasiado largo")
        String content,

        UUID topicId) {
}

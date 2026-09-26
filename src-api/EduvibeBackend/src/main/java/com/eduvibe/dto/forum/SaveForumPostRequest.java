package com.eduvibe.dto.forum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveForumPostRequest(

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = 10000, message = "El mensaje es demasiado largo")
        String content) {
}

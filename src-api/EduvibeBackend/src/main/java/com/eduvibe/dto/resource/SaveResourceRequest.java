package com.eduvibe.dto.resource;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveResourceRequest(

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String title,

        @Size(max = 2000, message = "El enlace es demasiado largo")
        String fileUrl,

        @Pattern(regexp = "pdf|link|video|doc|other",
                 message = "El tipo debe ser pdf, link, video, doc u other")
        String type,

        UUID topicId) {
}

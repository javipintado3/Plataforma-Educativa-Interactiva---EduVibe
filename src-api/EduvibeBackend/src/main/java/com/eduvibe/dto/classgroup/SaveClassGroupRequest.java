package com.eduvibe.dto.classgroup;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/** Un subgrupo completo, con todos sus miembros de una vez: se reemplazan enteros, no se editan uno a uno. */
public record SaveClassGroupRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @NotEmpty(message = "El subgrupo necesita al menos un miembro")
        List<UUID> memberIds) {
}

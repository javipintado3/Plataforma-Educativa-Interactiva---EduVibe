package com.eduvibe.dto.schoolclass;

import java.time.Instant;
import java.util.UUID;

import com.eduvibe.model.SchoolClass;

/**
 * Tarjeta de clase del panel principal.
 *
 * Incluye la próxima entrega porque es justo lo que hace útil esa pantalla: ver
 * de un vistazo qué toca, sin entrar en cada clase.
 *
 * @param miRol          papel de quien consulta dentro de la clase
 * @param proximaEntrega fecha de la siguiente tarea con plazo, o null si no hay
 * @param profesores     nombres del profesorado, para la línea bajo el título
 */
public record ClassResponse(
        UUID id,
        String name,
        String subject,
        String color,
        String imageUrl,
        String miRol,
        Instant proximaEntrega,
        java.util.List<String> profesores,
        Instant createdAt) {

    public static ClassResponse de(SchoolClass clase, String miRol,
                                   Instant proximaEntrega, java.util.List<String> profesores) {
        return new ClassResponse(
                clase.getId(),
                clase.getName(),
                clase.getSubject(),
                clase.getColor(),
                clase.getImageUrl(),
                miRol,
                proximaEntrega,
                profesores,
                clase.getCreatedAt());
    }
}

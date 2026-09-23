package com.eduvibe.dto.schoolclass;

import java.util.List;
import java.util.UUID;

import com.eduvibe.model.SchoolClass;

/**
 * Cabecera de la pantalla de una clase.
 *
 * @param puedoEditar   si quien consulta puede crear tareas y materiales aquí;
 *                      el cliente lo usa para decidir qué botones enseña
 * @param temas         bloques en los que se organiza el trabajo de clase
 * @param numeroAlumnos recuento, para no tener que traerse la lista entera
 */
public record ClassDetailResponse(
        UUID id,
        String name,
        String subject,
        String color,
        String miRol,
        boolean puedoEditar,
        List<MemberResponse> profesores,
        long numeroAlumnos,
        List<TopicResponse> temas) {

    public static ClassDetailResponse de(SchoolClass clase, String miRol, boolean puedoEditar,
                                         List<MemberResponse> profesores, long numeroAlumnos,
                                         List<TopicResponse> temas) {
        return new ClassDetailResponse(
                clase.getId(),
                clase.getName(),
                clase.getSubject(),
                clase.getColor(),
                miRol,
                puedoEditar,
                profesores,
                numeroAlumnos,
                temas);
    }
}

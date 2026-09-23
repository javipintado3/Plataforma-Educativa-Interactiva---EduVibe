package com.eduvibe.model.enums;

/**
 * Rol de un usuario dentro de la organización.
 *
 * Los valores se guardan tal cual en la columna users.role, que tiene una
 * restricción CHECK con esta misma lista. Si se añade un rol aquí hay que
 * añadirlo también en una migración.
 */
public enum UserRole implements EnumConValor {

    /** Da de alta y de baja usuarios, crea clases y gestiona la organización. */
    ADMIN("admin"),

    /** Crea tareas, materiales y avisos, y califica entregas. */
    TEACHER("teacher"),

    /** Ve sus clases, entrega tareas y consulta sus notas. */
    STUDENT("student"),

    /** Tutor legal: consulta el progreso de un alumno, sin poder editar nada. */
    GUARDIAN("guardian");

    private final String valor;

    UserRole(String valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        return valor;
    }

    public static UserRole desdeValor(String valor) {
        return EnumConValor.desde(UserRole.class, valor);
    }
}

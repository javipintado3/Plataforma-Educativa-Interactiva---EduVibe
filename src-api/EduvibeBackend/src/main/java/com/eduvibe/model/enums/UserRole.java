package com.eduvibe.model.enums;

/**
 * Rol de un usuario dentro de la organización.
 *
 * Los valores se guardan tal cual en la columna users.role, que tiene una
 * restricción CHECK con esta misma lista. Si se añade un rol aquí hay que
 * añadirlo también en una migración.
 */
public enum UserRole {

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

    /** Valor tal y como se almacena en base de datos y viaja en el JWT. */
    public String getValor() {
        return valor;
    }

    /**
     * Convierte el valor almacenado en el enumerado.
     *
     * @throws IllegalArgumentException si el valor no corresponde a ningún rol.
     */
    public static UserRole desdeValor(String valor) {
        for (UserRole rol : values()) {
            if (rol.valor.equalsIgnoreCase(valor)) {
                return rol;
            }
        }
        throw new IllegalArgumentException("Rol no reconocido: " + valor);
    }
}

package com.eduvibe.model.enums;

/**
 * Papel de una persona dentro de una clase concreta.
 *
 * Es independiente del rol en la organización: quien es TEACHER en el centro
 * solo imparte las clases en las que está matriculado como profesor. Una clase
 * admite varios profesores, y por eso esto vive en enrollments y no en una
 * columna de classes.
 */
public enum EnrollmentRole implements EnumConValor {

    TEACHER("teacher"),
    STUDENT("student");

    private final String valor;

    EnrollmentRole(String valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        return valor;
    }

    public static EnrollmentRole desdeValor(String valor) {
        return EnumConValor.desde(EnrollmentRole.class, valor);
    }
}

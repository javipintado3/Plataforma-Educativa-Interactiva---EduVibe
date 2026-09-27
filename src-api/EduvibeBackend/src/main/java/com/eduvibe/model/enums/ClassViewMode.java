package com.eduvibe.model.enums;

/**
 * Cómo se organiza el contenido de una clase.
 *
 * Decide qué pantalla ve quien entra en la clase: temas con actividades y
 * materiales separados (primaria/secundaria), o módulos con todo mezclado
 * (universidad). Se elige por clase, no globalmente para toda la plataforma.
 */
public enum ClassViewMode implements EnumConValor {

    STRUCTURED("structured"),
    FLEXIBLE("flexible");

    private final String valor;

    ClassViewMode(String valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        return valor;
    }

    public static ClassViewMode desdeValor(String valor) {
        return EnumConValor.desde(ClassViewMode.class, valor);
    }
}

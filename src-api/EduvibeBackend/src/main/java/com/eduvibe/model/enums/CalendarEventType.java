package com.eduvibe.model.enums;

/**
 * Tipo de evento introducido a mano en el calendario.
 *
 * Las entregas no están aquí: se derivan de la fecha límite de las tareas, para
 * no tener el mismo dato en dos sitios y que puedan dejar de coincidir.
 */
public enum CalendarEventType implements EnumConValor {

    EXAM("exam"),
    HOLIDAY("holiday"),
    OTHER("other");

    private final String valor;

    CalendarEventType(String valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        return valor;
    }

    public static CalendarEventType desdeValor(String valor) {
        return EnumConValor.desde(CalendarEventType.class, valor);
    }
}

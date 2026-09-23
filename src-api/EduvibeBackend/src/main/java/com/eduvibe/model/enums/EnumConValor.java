package com.eduvibe.model.enums;

/**
 * Enumerado que se guarda en base de datos con un valor propio, distinto del
 * nombre de la constante.
 *
 * Todos los enumerados del modelo lo son: en Java se escriben en mayúsculas,
 * como manda la convención, y en la base de datos en minúsculas, que es lo que
 * esperan las restricciones CHECK de las columnas. Esta interfaz permite que
 * una sola conversión sirva para todos.
 */
public interface EnumConValor {

    /** Valor tal y como se almacena y viaja por la API. */
    String getValor();

    /**
     * Busca la constante que corresponde a un valor almacenado.
     *
     * @throws IllegalArgumentException si no corresponde a ninguna, con la
     *         lista de las admitidas para que el mensaje sea útil.
     */
    static <E extends Enum<E> & EnumConValor> E desde(Class<E> tipo, String valor) {
        for (E constante : tipo.getEnumConstants()) {
            if (constante.getValor().equalsIgnoreCase(valor)) {
                return constante;
            }
        }
        StringBuilder admitidos = new StringBuilder();
        for (E constante : tipo.getEnumConstants()) {
            if (admitidos.length() > 0) {
                admitidos.append(", ");
            }
            admitidos.append(constante.getValor());
        }
        throw new IllegalArgumentException(
                "Valor no reconocido: '" + valor + "'. Los admitidos son: " + admitidos);
    }
}

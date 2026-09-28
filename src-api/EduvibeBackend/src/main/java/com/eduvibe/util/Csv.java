package com.eduvibe.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Lectura y escritura de CSV sin depender de una librería externa.
 *
 * Sigue RFC 4180 en lo justo que necesita el proyecto: campos entre comillas
 * cuando llevan coma, comilla o salto de línea, con las comillas internas
 * duplicadas. No soporta un campo con un salto de línea real dentro (poco
 * probable en nombre, email o rol); si hiciera falta ese caso, es el momento
 * de traer una librería de verdad en lugar de complicar esto.
 */
public final class Csv {

    private Csv() {
    }

    public static String fila(String... campos) {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                linea.append(',');
            }
            linea.append(escapar(campos[i]));
        }
        return linea.toString();
    }

    private static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        boolean necesitaComillas = valor.contains(",") || valor.contains("\"")
                || valor.contains("\n") || valor.contains("\r");
        String escapado = valor.replace("\"", "\"\"");
        return necesitaComillas ? "\"" + escapado + "\"" : escapado;
    }

    /** Cada fila ya separada en campos. La primera fila es la cabecera, si la hay. */
    public static List<List<String>> leer(Reader entrada) throws IOException {
        List<List<String>> filas = new ArrayList<>();

        try (BufferedReader lector = new BufferedReader(entrada)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.isBlank()) {
                    continue;
                }
                filas.add(parsearLinea(linea));
            }
        }
        return filas;
    }

    private static List<String> parsearLinea(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean entreComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);

            if (entreComillas) {
                if (c == '"') {
                    boolean comillaEscapada = i + 1 < linea.length() && linea.charAt(i + 1) == '"';
                    if (comillaEscapada) {
                        actual.append('"');
                        i++;
                    } else {
                        entreComillas = false;
                    }
                } else {
                    actual.append(c);
                }
            } else if (c == '"') {
                entreComillas = true;
            } else if (c == ',') {
                campos.add(actual.toString().trim());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString().trim());
        return campos;
    }
}

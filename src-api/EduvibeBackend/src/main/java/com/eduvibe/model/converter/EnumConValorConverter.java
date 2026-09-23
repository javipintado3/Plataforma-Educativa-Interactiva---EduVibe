package com.eduvibe.model.converter;

import com.eduvibe.model.enums.EnumConValor;

import jakarta.persistence.AttributeConverter;

/**
 * Conversión común para todos los enumerados del modelo.
 *
 * Cada enumerado solo necesita una subclase de tres líneas anotada con
 * {@code @Converter(autoApply = true)}, en lugar de repetir la misma pareja de
 * métodos una y otra vez. JPA exige que el convertidor declare tipos concretos,
 * y de ahí que las subclases existan; pero la lógica vive aquí.
 */
public abstract class EnumConValorConverter<E extends Enum<E> & EnumConValor>
        implements AttributeConverter<E, String> {

    private final Class<E> tipo;

    protected EnumConValorConverter(Class<E> tipo) {
        this.tipo = tipo;
    }

    @Override
    public String convertToDatabaseColumn(E constante) {
        return constante == null ? null : constante.getValor();
    }

    @Override
    public E convertToEntityAttribute(String valor) {
        return valor == null ? null : EnumConValor.desde(tipo, valor);
    }
}

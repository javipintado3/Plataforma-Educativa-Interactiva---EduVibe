package com.eduvibe.service;

import com.eduvibe.exception.BadRequestException;

/**
 * Qué se está subiendo: decide qué extensiones y qué tamaño máximo admite
 * {@link FileStorageService}. No es un enumerado de base de datos —nada lo
 * persiste—, así que no lleva el patrón EnumConValor de los demás.
 */
public enum TipoSubida {
    IMAGEN,
    DOCUMENTO;

    public static TipoSubida desdeValor(String valor) {
        if ("imagen".equalsIgnoreCase(valor)) {
            return IMAGEN;
        }
        if (valor == null || "documento".equalsIgnoreCase(valor)) {
            return DOCUMENTO;
        }
        throw new BadRequestException("El tipo debe ser imagen o documento");
    }
}

package com.eduvibe.exception;

import org.springframework.http.HttpStatus;

/** El recurso solicitado no existe. */
public class NotFoundException extends ApiException {

    public NotFoundException(String mensaje) {
        super(HttpStatus.NOT_FOUND, mensaje);
    }

    public static NotFoundException de(String entidad, Object id) {
        return new NotFoundException(entidad + " no encontrado: " + id);
    }
}

package com.example.EduvibeBackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Clase que representa una excepción global personalizada para la aplicación.
 * Esta excepción se lanza cuando ocurre un error específico definido por la aplicación.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class GlobalException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje de error.
     *
     * @param msg El mensaje de error que describe la causa de la excepción.
     */
    public GlobalException(String msg) {
        super(msg);
    }
}

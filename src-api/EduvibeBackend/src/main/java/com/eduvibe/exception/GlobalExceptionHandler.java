package com.eduvibe.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.eduvibe.dto.common.ErrorResponse;

/**
 * Convierte las excepciones en respuestas con la misma forma.
 *
 * Centralizarlo aquí evita que cada controlador invente su propio formato de
 * error, y sobre todo evita el problema de la versión anterior: una única
 * excepción para todo, anotada con un código HTTP fijo, que hacía que un fallo
 * de credenciales se devolviese como 404.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Errores propios de la aplicación: cada uno ya sabe su código. */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> manejarApiException(ApiException ex, WebRequest peticion) {
        return construir(ex.getStatus(), ex.getMessage(), peticion);
    }

    /** Credenciales incorrectas o cuenta no habilitada. */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> manejarCredenciales(BadCredentialsException ex, WebRequest peticion) {
        return construir(HttpStatus.UNAUTHORIZED, ex.getMessage(), peticion);
    }

    /**
     * Autenticado, pero sin permiso. Las denegaciones que ocurren en la cadena
     * de filtros las atiende RestAccessDeniedHandler; aquí llegan las que
     * lanza la seguridad a nivel de método.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> manejarAcceso(AccessDeniedException ex, WebRequest peticion) {
        return construir(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta operación", peticion);
    }

    /** Fallos de validación de los DTO: se devuelve el detalle por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex, WebRequest peticion) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(ErrorResponse.deValidacion(ruta(peticion), campos));
    }

    /**
     * Red de seguridad. Se registra la traza completa en el log pero al cliente
     * solo le llega un mensaje genérico: los detalles internos de un fallo no
     * previsto no deben salir de la aplicación.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarNoPrevista(Exception ex, WebRequest peticion) {
        LOG.error("Error no controlado en {}", ruta(peticion), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error inesperado", peticion);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje, WebRequest peticion) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.de(status.value(), status.getReasonPhrase(), mensaje, ruta(peticion)));
    }

    private String ruta(WebRequest peticion) {
        return peticion.getDescription(false).replaceFirst("^uri=", "");
    }
}

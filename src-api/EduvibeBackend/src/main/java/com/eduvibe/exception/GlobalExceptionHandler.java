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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
     * Cuerpo de la petición ilegible: JSON mal formado, mal codificado, o un
     * campo con un tipo que no encaja. Es culpa de quien llama, así que es un
     * 400; sin este manejador caía en la red de seguridad y se devolvía un 500,
     * que haría buscar el problema en el sitio equivocado.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarCuerpoIlegible(HttpMessageNotReadableException ex,
                                                               WebRequest peticion) {
        LOG.debug("Cuerpo de petición ilegible en {}: {}", ruta(peticion), ex.getMessage());
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no se ha podido interpretar. Revisa que sea JSON válido en UTF-8.",
                peticion);
    }

    /** Ruta inexistente: 404 y no 500. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> manejarRutaInexistente(NoResourceFoundException ex,
                                                                WebRequest peticion) {
        return construir(HttpStatus.NOT_FOUND, "No existe el recurso solicitado", peticion);
    }

    /**
     * Un parámetro de ruta con formato imposible, por ejemplo un identificador
     * que no es un UUID. También es culpa de quien llama.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarTipoIncorrecto(MethodArgumentTypeMismatchException ex,
                                                               WebRequest peticion) {
        return construir(HttpStatus.BAD_REQUEST,
                "El valor de '" + ex.getName() + "' no tiene el formato esperado", peticion);
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
